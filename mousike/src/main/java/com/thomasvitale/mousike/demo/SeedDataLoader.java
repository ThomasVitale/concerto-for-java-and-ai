package com.thomasvitale.mousike.demo;

import java.time.Duration;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.thomasvitale.mousike.notebook.Classification.Classified;
import com.thomasvitale.mousike.notebook.Classification.NeedsReview;
import com.thomasvitale.mousike.notebook.NoteKind;
import com.thomasvitale.mousike.notebook.NoteService;
import com.thomasvitale.mousike.scenes.Medium;
import com.thomasvitale.mousike.scenes.Scene;
import com.thomasvitale.mousike.scenes.SceneMarker;
import com.thomasvitale.mousike.scenes.SceneService;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Seeds the notebook on first start so the demo has something to search. Runs on a virtual thread; each seed
 * note goes through the real tagging pipeline. Off with {@code mousike.demo.seed=false}, as in tests, where the
 * context would close under the seeding thread.
 */
@Component
@ConditionalOnBooleanProperty(name = "mousike.demo.seed", matchIfMissing = true)
class SeedDataLoader {

    private static final Logger log = LoggerFactory.getLogger(SeedDataLoader.class);

    private final NoteService noteService;
    private final SceneService sceneService;
    private final JsonMapper jsonMapper;
    private final AsyncTaskExecutor executor;

    SeedDataLoader(NoteService noteService, SceneService sceneService, JsonMapper jsonMapper,
            @Qualifier("applicationTaskExecutor") AsyncTaskExecutor executor) {
        this.noteService = noteService;
        this.sceneService = sceneService;
        this.jsonMapper = jsonMapper;
        this.executor = executor;
    }

    @EventListener(ApplicationReadyEvent.class)
    void seedWhenEmpty() {
        if (sceneService.count() == 0) {
            seedScenes();
        }
        if (noteService.count() == 0) {
            executor.execute(this::seedNotes);
        }
    }

    private void seedScenes() {
        List<SeedScene> seeds;
        try (InputStream in = new ClassPathResource("demo/scenes.json").getInputStream()) {
            seeds = jsonMapper.readValue(in, new TypeReference<List<SeedScene>>() { });
        }
        catch (IOException ex) {
            log.warn("Could not read seed scenes", ex);
            return;
        }
        seeds.forEach(seed -> sceneService.save(Scene.create(seed.production(), seed.title(), seed.description(), seed.medium(), seed.genre(), null,
                seed.markers().stream().map(m -> new SceneMarker(m.timeMarker(), m.comment())).toList())));
        log.info("Seeded {} scenes", seeds.size());
    }

    private void seedNotes() {
        List<SeedNote> seeds;
        try (InputStream in = new ClassPathResource("demo/notes.json").getInputStream()) {
            seeds = jsonMapper.readValue(in, new TypeReference<List<SeedNote>>() { });
        }
        catch (IOException ex) {
            log.warn("Could not read seed notes", ex);
            return;
        }
        log.info("Seeding {} notebook notes through the decision model…", seeds.size());
        long start = System.nanoTime();
        int saved = 0;
        for (SeedNote seed : seeds) {
            try {
                // The demo never stalls at startup: if the model is unsure about a seed, keep the kind it was written for
                switch (noteService.create(seed.content())) {
                    case Classified classified when classified.note().kind() != seed.kind() ->
                            log.warn("Seed note classified as {}, written for {}: '{}'", classified.note().kind(), seed.kind(),
                                    seed.content());
                    case Classified _ -> { }
                    case NeedsReview review -> {
                        log.warn("Unsure about seed note '{}' ({}); keeping {}", seed.content(), review.candidates(), seed.kind());
                        noteService.create(seed.content(), seed.kind());
                    }
                }
                saved++;
            }
            catch (RuntimeException ex) {
                log.warn("Could not seed note '{}': {}", seed.content(), ex.getMessage());
            }
        }
        log.info("Seeded {} notes in {}", saved, Duration.ofNanos(System.nanoTime() - start));
    }

    /** A demo note and the kind it is written to land on; {@code NoteClassificationCalibrationTests} checks both. */
    record SeedNote(NoteKind kind, String content) {
    }

    record SeedScene(@Nullable String production, String title, String description, Medium medium, String genre, List<SeedMarker> markers) {
    }

    record SeedMarker(String timeMarker, String comment) {
    }

}
