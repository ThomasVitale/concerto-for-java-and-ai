package com.thomasvitale.mousike.chords;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Serves a progression as a Standard MIDI File, for the browser player, and for downloading into a DAW.
 */
@RestController
class ChordProgressionController {

    private final ChordProgressionService progressionService;

    ChordProgressionController(ChordProgressionService progressionService) {
        this.progressionService = progressionService;
    }

    @GetMapping(path = "/progressions/{id}.mid", produces = "audio/midi")
    ResponseEntity<byte[]> midi(@PathVariable UUID id) {
        return progressionService.findById(id)
                .map(progression -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("audio/midi"))
                        .headers(headers -> headers.setContentDisposition(ContentDisposition.inline()
                                .filename(progression.title() + ".mid", StandardCharsets.UTF_8)
                                .build()))
                        .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                        .body(progression.midi()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
