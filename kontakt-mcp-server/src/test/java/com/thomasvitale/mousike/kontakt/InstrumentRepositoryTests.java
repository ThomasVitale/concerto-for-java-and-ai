package com.thomasvitale.mousike.kontakt;

import com.thomasvitale.mousike.kontakt.instrument.Instrument;
import com.thomasvitale.mousike.kontakt.instrument.InstrumentRepository;
import com.thomasvitale.mousike.kontakt.instrument.InstrumentTag;
import com.thomasvitale.mousike.kontakt.instrument.PresetCategory;
import com.thomasvitale.mousike.kontakt.instrument.Tag;
import com.thomasvitale.mousike.kontakt.instrument.Library;
import com.thomasvitale.mousike.kontakt.instrument.InstrumentSummary;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InstrumentRepositoryTests {

    @TempDir
    static Path directory;

    @Autowired
    InstrumentRepository repository;

    @BeforeAll
    static void createFixture() throws Exception {
        KontaktFixture.create(directory);
    }

    @DynamicPropertySource
    static void kontaktDatabase(DynamicPropertyRegistry registry) {
        registry.add("kontakt.database", () -> directory.resolve("komplete.db3").toString());
    }

    @Test
    void searchMatchesWordsAcrossNameProductAndType() {
        List<Instrument> cello = repository.search("cello", null, null, 10);
        assertThat(cello).extracting(Instrument::name).containsExactly("AS Tina Guo Legato");
        assertThat(cello.getFirst().authorOrBrand()).isEqualTo("Cinesamples, Tina Guo");
        assertThat(cello.getFirst().category()).isEqualTo("Instruments");
        assertThat(cello.getFirst().format()).isEqualTo("Instrument");
    }

    @Test
    void searchNeedsEveryWordFirstThenFallsBackToAnyWord() {
        assertThat(repository.search("strings low", null, null, 10)).extracting(Instrument::name).containsExactly("Strings Low Long");
        // "warm" matches nothing, so any word may match: both Albion presets and the cello (its Sound Type says: "Bowed Strings")
        assertThat(repository.search("strings warm", null, null, 10)).extracting(Instrument::name)
                .containsExactlyInAnyOrder("Strings Low Long", "Strings High Long", "AS Tina Guo Legato");
    }

    @Test
    void searchFiltersBySoundTypeCharacterAndCategory() {
        assertThat(repository.search("long", "Ensemble", null, 10)).hasSize(2);
        assertThat(repository.search("long", "Cello", null, 10)).isEmpty();
        assertThat(repository.search("felt", null, "dark", 10)).extracting(Instrument::format).containsExactly("Snapshot");
        assertThat(repository.search("felt", null, null, PresetCategory.COMBINED, null, 10)).isEmpty();
        assertThat(repository.search("brass", null, null, PresetCategory.TOOLS, null, 10)).extracting(Instrument::name).containsExactly("Brass Swell Phrase");
    }

    @Test
    void searchWithinProducts() {
        assertThat(repository.search("strings", null, null, null, List.of("Albion ONE"), 10)).extracting(Instrument::name)
                .containsExactlyInAnyOrder("Strings Low Long", "Strings High Long");
        assertThat(repository.search("strings", null, null, null, List.of(), 10)).isEmpty();
        assertThat(repository.search("strings", null, null, null, null, 10)).hasSize(3);
    }

    @Test
    void searchLeavesOutToolsUnlessAskedFor() {
        assertThat(repository.search("brass", null, null, 10)).extracting(Instrument::name).containsExactly("Brass Ensemble");
        assertThat(repository.findById(6L)).map(Instrument::category).contains("Tools");
        assertThat(repository.findById(6L)).map(Instrument::format).contains("Tool preset");
    }

    @Test
    void tagsOneByOne() {
        // the view joins the cello's two Sound Types into "Bowed Strings, Cello, Bowed Strings, Ensemble"
        assertThat(repository.soundTypes(List.of(3L, 5L))).containsExactly(new InstrumentTag(3, "Bowed Strings, Cello"),
                new InstrumentTag(3, "Bowed Strings, Ensemble"), new InstrumentTag(5, "Piano / Keys, Grand Piano"));
        assertThat(repository.characters(List.of(5L))).extracting(InstrumentTag::name).containsExactly("Dark", "Evolving");
        assertThat(repository.characters(List.of())).isEmpty();
    }

    @Test
    void searchIgnoresPunctuationAndBlankQueries() {
        assertThat(repository.search("  ", null, null, 10)).isEmpty();
        assertThat(repository.search("brass AND (", null, null, 10)).extracting(Instrument::name).containsExactly("Brass Ensemble");
        assertThat(InstrumentRepository.terms("solo cello legato")).containsExactly("\"solo\"*", "\"cello\"*", "\"legato\"*");
    }

    @Test
    void derivedQueriesReadTheView() {
        assertThat(repository.count()).isEqualTo(6);
        assertThat(repository.findById(3L)).map(Instrument::name).contains("AS Tina Guo Legato");
        assertThat(repository.findByProductOrderByName("Albion ONE")).extracting(Instrument::name)
                .containsExactly("Strings High Long", "Strings Low Long");
    }

    @Test
    void librariesSoundTypesCharactersAndSummaryComeFromTheCatalogue() {
        assertThat(repository.libraries()).extracting(Library::product).containsExactly("Albion ONE", "Kontakt", "Artist Series - Tina Guo Acoustic Cello Legato", "Noire");
        assertThat(repository.libraries().getFirst()).isEqualTo(new Library("Albion ONE", "Spitfire Audio", 2, "1.5.0.0", directory.resolve("Albion ONE").toString()));
        assertThat(repository.libraries().get(1).folder()).isEqualTo("/Volumes/Unmounted Drive/Libraries");
        assertThat(repository.soundTypes()).first().isEqualTo(new Tag("Bowed Strings, Ensemble", 3));
        assertThat(repository.soundTypes()).extracting(Tag::name).contains("Brass, Ensemble");
        assertThat(repository.characters()).extracting(Tag::name).containsExactly("Acoustic", "Dark", "Evolving");
        assertThat(repository.summary()).isEqualTo(new InstrumentSummary(6, 4, 4, 3));
    }

}
