package com.thomasvitale.mousike.kontakt.catalog;

import com.thomasvitale.mousike.kontakt.config.KontaktProperties;
import com.thomasvitale.mousike.kontakt.instrument.Instrument;
import com.thomasvitale.mousike.kontakt.instrument.InstrumentRepository;
import com.thomasvitale.mousike.kontakt.instrument.InstrumentTag;
import com.thomasvitale.mousike.kontakt.instrument.Library;
import com.thomasvitale.mousike.kontakt.instrument.PresetCategory;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class Catalog {

    private final InstrumentRepository instruments;
    private final KontaktProperties properties;

    Catalog(InstrumentRepository instruments, KontaktProperties properties) {
        this.instruments = instruments;
        this.properties = properties;
    }

    /**
     * Best matches first, the ones the composer can load right now ahead of the offline ones.
     * With {@code availableOnly} the search runs within the libraries on disk,
     * so the limit still counts loadable presets.
     *
     * @param category a Browser category label, e.g. "Instruments"; null for every category but Tools
     * @throws IllegalArgumentException when the category is not one of the Browser's
     */
    public SearchResult search(String query, @Nullable String soundType, @Nullable String character, @Nullable String category,
            @Nullable Integer limit, boolean availableOnly) {
        PresetCategory within = category == null || category.isBlank() ? null : PresetCategory.of(category).orElseThrow(() ->
                new IllegalArgumentException("'%s' is not a Kontakt Browser category. Use one of: %s.".formatted(category,
                        Arrays.stream(PresetCategory.values()).map(PresetCategory::label).collect(Collectors.joining(", ")))));
        List<String> products = availableOnly
                ? libraries().stream().filter(LibraryInfo::available).map(LibraryInfo::product).toList()
                : null;
        List<InstrumentMatch> matches = matches(instruments.search(query, soundType, character, within, products,
                limit == null ? InstrumentRepository.DEFAULT_LIMIT : limit)).stream()
                .sorted(Comparator.comparing(match -> !match.available()))
                .toList();
        if (matches.isEmpty() && availableOnly) {
            return new SearchResult(matches, "No available Kontakt preset matches '%s'. Search again with availableOnly false to see presets on drives that are not mounted, or use plainer words.".formatted(query));
        }
        if (matches.isEmpty()) {
            return new SearchResult(matches, "No installed Kontakt preset matches '%s'. Do not repeat this search: use one or two plainer words (the instrument's name, e.g. 'piano' or 'cello'), or filter by Sound Type or Character instead.".formatted(query));
        }
        long offline = matches.stream().filter(match -> !match.available()).count();
        String found = matches.size() == 1 ? "1 matching preset." : "%d matching presets, best first.".formatted(matches.size());
        String note = offline == 0
                ? found
                : "%s %d of them sit on a drive that is not mounted, listed last: prefer the available ones.".formatted(found, offline);
        return new SearchResult(matches, note);
    }

    public List<LibraryInfo> libraries() {
        return instruments.libraries().stream().map(LibraryInfo::of).toList();
    }

    public Optional<LibraryDetails> library(String product) {
        return instruments.libraries().stream()
                .filter(library -> library.product().equals(product))
                .findFirst()
                .map(this::details);
    }

    public List<InstrumentMatch> instruments(String product) {
        return matches(instruments.findByProductOrderByName(product));
    }

    public Optional<InstrumentDetails> instrument(long id) {
        return instruments.findById(id).map(this::details);
    }

    /**
     * The manual's file, only when it is one of the library's listed manuals,
     * so no other path can be read.
     */
    public Optional<Path> manual(String product, String name) {
        return library(product).stream()
                .flatMap(details -> details.manuals().stream())
                .filter(manual -> manual.name().equals(name))
                .map(manual -> Path.of(manual.path()))
                .findFirst();
    }

    /**
     * Kontakt keeps an entry's preview next to it: {@code <folder>/.previews/<file name>.ogg}.
     */
    public Optional<Path> preview(long id) {
        return instruments.findById(id).flatMap(Catalog::preview);
    }

    private static Optional<Path> preview(Instrument instrument) {
        Path file = Path.of(instrument.fileName());
        Path parent = file.getParent();
        if (parent == null) {
            return Optional.empty();
        }
        Path preview = parent.resolve(".previews").resolve(file.getFileName() + ".ogg");
        return Files.isRegularFile(preview) ? Optional.of(preview) : Optional.empty();
    }

    private InstrumentDetails details(Instrument instrument) {
        LibraryDetails library = library(instrument.product()).orElseThrow();
        Preview preview = preview(instrument)
                .map(file -> new Preview(file.toString(), link("instruments", String.valueOf(instrument.id()), "preview")))
                .orElse(null);
        return new InstrumentDetails(matches(List.of(instrument)).getFirst(), instrument.comment(), instrument.fileName(), preview, library);
    }

    /**
     * Hits with their tags one by one, two queries for the whole list.
     */
    private List<InstrumentMatch> matches(List<Instrument> found) {
        List<Long> ids = found.stream().map(Instrument::id).toList();
        Map<Long, List<String>> soundTypes = byInstrument(instruments.soundTypes(ids));
        Map<Long, List<String>> characters = byInstrument(instruments.characters(ids));
        return found.stream()
                .map(instrument -> InstrumentMatch.of(instrument, soundTypes.getOrDefault(instrument.id(), List.of()),
                        characters.getOrDefault(instrument.id(), List.of())))
                .toList();
    }

    private static Map<Long, List<String>> byInstrument(List<InstrumentTag> tags) {
        return tags.stream().collect(Collectors.groupingBy(InstrumentTag::instrument,
                Collectors.mapping(InstrumentTag::name, Collectors.toList())));
    }

    private LibraryDetails details(Library library) {
        List<Manual> manuals = library.folder() == null ? List.of() : documents(Path.of(library.folder())).stream()
                .map(file -> new Manual(file.getFileName().toString(), file.toString(),
                        link("libraries", library.product(), "manuals", file.getFileName().toString())))
                .toList();
        return new LibraryDetails(LibraryInfo.of(library), library.folder(), manuals);
    }

    /**
     * A link to this server's REST API.
     */
    private URI link(String... segments) {
        return UriComponentsBuilder.fromUri(properties.url()).pathSegment("api").pathSegment(segments).build().encode().toUri();
    }

    /**
     * PDFs in the library's Documentation folder and one level below it
     * (some vendors sort them by language).
     */
    private static List<Path> documents(Path folder) {
        Path documentation = folder.resolve("Documentation");
        if (!Files.isDirectory(documentation)) {
            return List.of();
        }
        try (Stream<Path> files = Files.walk(documentation, 2)) {
            return files.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf"))
                    .sorted()
                    .toList();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
