package com.thomasvitale.mousike.compositionplan.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.thomasvitale.mousike.chords.ChordProgressionService;
import com.thomasvitale.mousike.compositionplan.CompositionSheet;
import com.thomasvitale.mousike.notebook.Note;
import com.thomasvitale.mousike.notebook.NoteKind;
import com.thomasvitale.mousike.notebook.NoteService;
import com.thomasvitale.mousike.scenes.Scene;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * The plan's first step: the harmony note closest to the scene gives the first progression, spelled by Java so no chord gets
 * miscopied, and the model suggests the others. The composer sees where each comes from.
 */
@Component
public class ChordProgressionFinder {

    private static final Logger log = LoggerFactory.getLogger(ChordProgressionFinder.class);

    private static final int PROGRESSIONS = 3;

    /** Room for ideas: the suggestions should not be the same every time. */
    private static final double TEMPERATURE = 0.7;

    private final ChatClient chatClient;
    private final NoteService noteService;
    private final ChordProgressionService progressions;

    ChordProgressionFinder(ChatClient.Builder builder, NoteService noteService, ChordProgressionService progressions) {
        this.chatClient = PlanDraft.structured(builder, TEMPERATURE).build();
        this.noteService = noteService;
        this.progressions = progressions;
    }

    public List<CompositionSheet.HarmonicIdea> find(Scene scene, Consumer<String> progress) {
        progress.accept("Looking through your notes for chord progressions");
        List<String> notes = noteService.all().stream().map(Note::content).toList();
        List<CompositionSheet.HarmonicIdea> harmony = new ArrayList<>();
        Set<List<String>> seen = new HashSet<>();
        Optional<String> closest = noteService.searchByMeaning(PlanPrompts.describe(scene), NoteKind.HARMONY, 1).stream()
                .map(hit -> hit.note().content())
                .findFirst();
        closest.ifPresent(note -> {
            List<String> chords = ProgressionCheck.chordsIn(note);
            if (seen.add(chords) && !chords.isEmpty()) {
                String key = ProgressionCheck.key(chords, null, notes);
                String name = key + ": " + String.join(" ", chords);
                harmony.add(new CompositionSheet.HarmonicIdea(name, key, chords, note, render(name, key, chords, progress), true));
            }
        });
        PlanDraft.Harmony draft = chatClient.prompt()
                .system(PlanPrompts.HARMONY)
                .user("%s\n\nThe composer already has: %s\nSuggest %d other progressions.".formatted(PlanPrompts.describe(scene),
                        closest.orElse("nothing yet"), PROGRESSIONS - harmony.size()))
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .call()
                .entity(PlanDraft.Harmony.class);
        for (PlanDraft.Progression idea : draft == null ? List.<PlanDraft.Progression>of() : PlanDraft.nullSafe(draft.progressions())) {
            List<String> chords = PlanDraft.nullSafe(idea.chords()).stream().map(String::strip).filter(chord -> !chord.isEmpty()).toList();
            if (chords.isEmpty() || !seen.add(chords) || harmony.size() == PROGRESSIONS) {
                continue;
            }
            String key = ProgressionCheck.key(chords, idea.key(), notes);
            String name = title(idea.name(), key, chords);
            harmony.add(new CompositionSheet.HarmonicIdea(name, key, chords, PlanDraft.orElse(idea.why(), ""), render(name, key, chords, progress),
                    ProgressionCheck.isInNotes(chords, notes)));
        }
        return harmony;
    }

    private @Nullable UUID render(String name, String key, List<String> chords, Consumer<String> progress) {
        try {
            progress.accept("Preparing “" + name + "”: " + String.join(" ", chords));
            return progressions.render(name, key, chords, ChordProgressionService.DEFAULT_TEMPO,
                    ChordProgressionService.DEFAULT_BEATS_PER_CHORD).requiredId();
        }
        catch (RuntimeException ex) {
            log.debug("Could not render '{}' ({}): {}", name, chords, ex.getMessage());
            progress.accept("Could not prepare “" + name + "”: " + ex.getMessage());
            return null;
        }
    }

    /** A card title stays short: a sentence from the model becomes "key: chords". */
    private static String title(@Nullable String name, String key, List<String> chords) {
        String fallback = (key.isBlank() ? "" : key + ": ") + String.join(" ", chords);
        return PlanDraft.blank(name) || name.strip().length() > 40 ? fallback : name.strip();
    }

}
