package com.thomasvitale.mousike.kontakt.web;

import com.thomasvitale.mousike.kontakt.catalog.Catalog;
import com.thomasvitale.mousike.kontakt.catalog.InstrumentDetails;
import com.thomasvitale.mousike.kontakt.catalog.InstrumentMatch;
import com.thomasvitale.mousike.kontakt.catalog.LibraryDetails;
import com.thomasvitale.mousike.kontakt.catalog.LibraryInfo;
import com.thomasvitale.mousike.kontakt.catalog.SearchResult;
import com.thomasvitale.mousike.kontakt.instrument.InstrumentRepository;
import com.thomasvitale.mousike.kontakt.instrument.InstrumentSummary;
import com.thomasvitale.mousike.kontakt.instrument.Tag;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
class KontaktController {

    private static final MediaType OGG = MediaType.parseMediaType("audio/ogg");

    private final Catalog catalog;
    private final InstrumentRepository instruments;

    KontaktController(Catalog catalog, InstrumentRepository instruments) {
        this.catalog = catalog;
        this.instruments = instruments;
    }

    @GetMapping("/summary")
    InstrumentSummary summary() {
        return instruments.summary();
    }

    @GetMapping("/libraries")
    List<LibraryInfo> libraries() {
        return catalog.libraries();
    }

    @GetMapping("/libraries/{product}")
    ResponseEntity<LibraryDetails> library(@PathVariable String product) {
        return ResponseEntity.of(catalog.library(product));
    }

    @GetMapping("/libraries/{product}/instruments")
    List<InstrumentMatch> libraryInstruments(@PathVariable String product) {
        return catalog.instruments(product);
    }

    @GetMapping("/libraries/{product}/manuals/{name}")
    ResponseEntity<Resource> manual(@PathVariable String product, @PathVariable String name) {
        return catalog.manual(product, name)
                .map(file -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(name).build().toString())
                        .<Resource>body(new FileSystemResource(file)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/instruments")
    SearchResult search(@RequestParam String q, @RequestParam(required = false) @Nullable String soundType,
            @RequestParam(required = false) @Nullable String character, @RequestParam(required = false) @Nullable String category,
            @RequestParam(required = false) @Nullable Integer limit, @RequestParam(defaultValue = "true") boolean availableOnly) {
        return catalog.search(q, soundType, character, category, limit, availableOnly);
    }

    @GetMapping("/instruments/{id}")
    ResponseEntity<InstrumentDetails> instrument(@PathVariable long id) {
        return ResponseEntity.of(catalog.instrument(id));
    }

    /** Spring MVC answers Range requests for a Resource body, which browsers use to seek in audio. */
    @GetMapping("/instruments/{id}/preview")
    ResponseEntity<Resource> preview(@PathVariable long id) {
        return catalog.preview(id)
                .map(file -> ResponseEntity.ok().contentType(OGG).<Resource>body(new FileSystemResource(file)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sound-types")
    List<Tag> soundTypes() {
        return instruments.soundTypes();
    }

    @GetMapping("/characters")
    List<Tag> characters() {
        return instruments.characters();
    }

    /** An unknown Browser category, for one. */
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

}
