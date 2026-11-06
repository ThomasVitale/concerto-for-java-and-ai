package com.thomasvitale.mousike.compositionplan.internal;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.thomasvitale.mousike.compositionplan.CompositionSheet;
import com.thomasvitale.mousike.notebook.NoteKind;
import com.thomasvitale.mousike.notebook.NoteService;
import com.thomasvitale.mousike.scenes.Scene;
import com.thomasvitale.mousike.scenes.SceneMarker;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

/**
 * The plan's last step: from the sounds and progressions the first two steps chose and the composer's notes on arranging,
 * the model suggests the orchestration and arrangement at each time marker.
 */
@Component
public class OrchestrationPlanner {

    /** Room for ideas: the suggestions should not be the same every time. */
    private static final double TEMPERATURE = 0.5;

    private final ChatClient chatClient;
    private final NoteService noteService;

    OrchestrationPlanner(ChatClient.Builder builder, NoteService noteService) {
        this.chatClient = PlanDraft.structured(builder, TEMPERATURE).build();
        this.noteService = noteService;
    }

    public Suggestions suggest(Scene scene, List<CompositionSheet.PaletteEntry> palette, List<CompositionSheet.HarmonicIdea> harmony, Consumer<String> progress) {
        progress.accept("Suggesting the orchestration for each time marker");
        String brief = describe(scene, palette, harmony);
        ResponseEntity<ChatResponse, PlanDraft.Arc> response = ask(brief);
        if (hasEmptyMoment(response.entity())) {
            // Now and then the small model writes only the time marker where the text belongs: one more try is enough
            response = ask(brief);
        }
        PlanDraft.Arc arc = response.entity() == null ? new PlanDraft.Arc("", List.of()) : response.entity();
        List<CompositionSheet.OrchestrationStep> steps = PlanDraft.nullSafe(arc.moments()).stream()
                .filter(moment -> !PlanDraft.blank(moment.instruction()))
                .map(moment -> new CompositionSheet.OrchestrationStep(PlanDraft.orElse(moment.timeMarker(), ""), moment.instruction().strip(),
                        PlanDraft.nullSafe(moment.instruments()).stream().map(String::strip).filter(sound -> !sound.isEmpty()).toList()))
                .toList();
        return new Suggestions(PlanDraft.orElse(arc.concept(), ""), steps, modelName(response.response()));
    }

    private ResponseEntity<ChatResponse, PlanDraft.Arc> ask(String brief) {
        return chatClient.prompt()
                .system(PlanPrompts.ARC)
                .user(brief)
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .call()
                .responseEntity(PlanDraft.Arc.class);
    }

    private static boolean hasEmptyMoment(PlanDraft.@Nullable Arc arc) {
        return arc == null || PlanDraft.nullSafe(arc.moments()).stream()
                .anyMatch(moment -> PlanDraft.blank(moment.instruction()) || moment.instruction().chars().noneMatch(Character::isLetter));
    }

    private String describe(Scene scene, List<CompositionSheet.PaletteEntry> palette, List<CompositionSheet.HarmonicIdea> harmony) {
        List<String> approach = Stream.concat(notes(scene, NoteKind.ARRANGEMENT, 1), notes(scene, NoteKind.ORCHESTRATION, 2)).toList();
        return "%s\n\nSounds:\n%s\n\nChord progressions:\n%s\n\nThe composer's notes on orchestration and arrangement:\n%s\n\nWrite exactly %d moments, one for each of these time markers: %s.".formatted(PlanPrompts.describe(scene),
                PlanPrompts.bullets(palette.stream().map(entry -> entry.role() + ": " + entry.instrument()).toList()),
                PlanPrompts.bullets(harmony.stream().map(idea -> String.join(" ", idea.chords())).toList()),
                PlanPrompts.bullets(approach), scene.markers().size(),
                scene.markers().stream().map(SceneMarker::timeMarker).collect(Collectors.joining(", ")));
    }

    private Stream<String> notes(Scene scene, NoteKind kind, int limit) {
        return noteService.searchByMeaning(PlanPrompts.describe(scene), kind, limit).stream().map(hit -> hit.note().content());
    }

    private static String modelName(@Nullable ChatResponse response) {
        String model = response == null ? null : response.getMetadata().getModel();
        return model == null || model.isBlank() ? "chat model" : model;
    }

    public record Suggestions(String concept, List<CompositionSheet.OrchestrationStep> steps, String model) {
    }

}
