package com.thomasvitale.mousike.compositionplan;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.thomasvitale.mousike.compositionplan.internal.ChordProgressionFinder;
import com.thomasvitale.mousike.compositionplan.internal.CompositionPlanRepository;
import com.thomasvitale.mousike.compositionplan.internal.InstrumentFinder;
import com.thomasvitale.mousike.compositionplan.internal.OrchestrationPlanner;
import com.thomasvitale.mousike.scenes.Scene;
import com.thomasvitale.mousike.scenes.SceneService;
import org.springframework.stereotype.Service;

/**
 * Plans the music for a scene in three steps, each with its own {@code ChatClient}: chord progressions, instruments, then the
 * orchestration and arrangement at each time marker from what the first two chose. Each starts from the composer's notes
 * and the model adds its own ideas on top.
 */
@Service
public class CompositionPlanner {

    private final ChordProgressionFinder chordProgressions;
    private final InstrumentFinder instruments;
    private final OrchestrationPlanner orchestration;
    private final SceneService sceneService;
    private final CompositionPlanRepository plans;

    CompositionPlanner(ChordProgressionFinder chordProgressions, InstrumentFinder instruments, OrchestrationPlanner orchestration, SceneService sceneService,
            CompositionPlanRepository plans) {
        this.chordProgressions = chordProgressions;
        this.instruments = instruments;
        this.orchestration = orchestration;
        this.sceneService = sceneService;
        this.plans = plans;
    }

    /**
     * Plans the music for a scene and saves the result. Progress lines arrive on the calling thread as the
     * planner works, one per tool call or step. Not transactional on purpose: it spans several model calls, and a
     * progression that fails to render must not undo the progressions that did.
     */
    public CompositionPlan plan(UUID sceneId, Consumer<String> progress) {
        Scene scene = sceneService.findById(sceneId).orElseThrow(() -> new IllegalArgumentException("No scene " + sceneId));
        progress.accept("Reading the scene “" + scene.title() + "”");

        long start = System.nanoTime();
        List<CompositionSheet.HarmonicIdea> harmony = chordProgressions.find(scene, progress);
        Duration harmonyDuration = Duration.ofNanos(System.nanoTime() - start);

        start = System.nanoTime();
        List<CompositionSheet.PaletteEntry> palette = instruments.find(scene, progress);
        Duration gatherDuration = Duration.ofNanos(System.nanoTime() - start);

        start = System.nanoTime();
        OrchestrationPlanner.Suggestions suggestions = orchestration.suggest(scene, palette, harmony, progress);
        Duration structureDuration = harmonyDuration.plus(Duration.ofNanos(System.nanoTime() - start));

        CompositionSheet sheet = new CompositionSheet(suggestions.concept(), palette, harmony, suggestions.steps(), gatherDuration, structureDuration);
        CompositionPlan saved = plans.save(new CompositionPlan(null, scene.requiredId(), suggestions.model(), sheet, null));
        progress.accept("Plan ready");
        return saved;
    }

    public Optional<CompositionPlan> latestFor(UUID sceneId) {
        return plans.findFirstBySceneIdOrderByCreatedAtDesc(sceneId);
    }

}
