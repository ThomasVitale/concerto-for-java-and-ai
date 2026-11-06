package com.thomasvitale.mousike.notebook;

import com.thomasvitale.mousike.config.DataConfig;
import com.thomasvitale.mousike.notebook.internal.NoteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
class NoteRepositoryTests {

    @Autowired
    NoteRepository notes;

    @Test
    void savesAndReloadsNote() {
        Note saved = notes.save(new Note(null, "Celesta for fragile magic", NoteKind.INSTRUMENT, "d1-omni:600m",
                Duration.ofMillis(182), null, null));

        Note reloaded = notes.findById(saved.requiredId()).orElseThrow();

        assertThat(reloaded.kind()).isEqualTo(NoteKind.INSTRUMENT);
        assertThat(reloaded.taggingDuration()).isEqualTo(Duration.ofMillis(182));
        assertThat(reloaded.createdAt()).isNotNull();
        assertThat(reloaded.updatedAt()).isNotNull();
    }

    @Test
    void savesNoteFiledByHand() {
        Note saved = notes.save(Note.create("Celesta for fragile magic", NoteKind.INSTRUMENT));

        Note reloaded = notes.findById(saved.requiredId()).orElseThrow();

        assertThat(reloaded.kind()).isEqualTo(NoteKind.INSTRUMENT);
        assertThat(reloaded.taggedBy()).isNull();
        assertThat(reloaded.taggingDuration()).isNull();
    }

    @Test
    void countsNotesByKind() {
        notes.saveAll(List.of(
                new Note(null, "Celesta for fragile magic", NoteKind.INSTRUMENT, "test", Duration.ZERO, null, null),
                new Note(null, "Tin whistle for fairytales", NoteKind.INSTRUMENT, "test", Duration.ZERO, null, null),
                new Note(null, "ii-V-I in minor, rubato", NoteKind.HARMONY, "test", Duration.ZERO, null, null)));

        assertThat(notes.countByKind()).containsExactlyInAnyOrder(
                new KindCount(NoteKind.INSTRUMENT, 2), new KindCount(NoteKind.HARMONY, 1));
    }

    @TestConfiguration(proxyBeanMethods = false)
    @Import(DataConfig.class)
    static class AuditingConfig {
    }

}
