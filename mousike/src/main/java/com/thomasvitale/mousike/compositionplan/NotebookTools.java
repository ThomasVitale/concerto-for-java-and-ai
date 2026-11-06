package com.thomasvitale.mousike.compositionplan;

import java.util.List;
import java.util.Locale;

import com.thomasvitale.mousike.notebook.NoteKind;
import com.thomasvitale.mousike.notebook.NoteService;
import com.thomasvitale.mousike.notebook.ScoredNote;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * The composer's notes as a tool for the planner: semantic search, so "something unsettling under dialogue"
 * finds unsettling notes without those words.
 */
@Component
public class NotebookTools {

    private final NoteService notes;

    public NotebookTools(NoteService notes) {
        this.notes = notes;
    }

    @Tool(name = "searchNotebook", description = """
            Search the composer's own notes by meaning: harmony, instruments, orchestration, arrangement, sound \
            design, styles and techniques they have written down. Ask in plain words about a mood, a scene or an \
            instrument, e.g. 'slow build of tension', 'cheerful fairytale instruments', 'low drone under dialogue'. \
            Returns the matching notes with their kind. Every idea you propose must come from these notes.""")
    public NotebookSearch searchNotebook(
            @ToolParam(description = "What to look for, in plain words") String query,
            @ToolParam(description = "Limit to one kind of note: HARMONY, MELODY, RHYTHM, INSTRUMENT, ORCHESTRATION, ARRANGEMENT, SOUND_DESIGN, STYLE or TECHNIQUE", required = false) @Nullable String kind) {
        NoteKind noteKind = parseKind(kind);
        List<NotebookHit> hits = notes.searchByMeaning(query, noteKind, 6).stream().map(NotebookHit::of).toList();
        String note = hits.isEmpty()
                ? "The composer has no note close to '%s'. Do not repeat this search: ask about a mood or an instrument in other words, or say the notebook has nothing for it.".formatted(query)
                : "%d notes, closest first.".formatted(hits.size());
        return new NotebookSearch(hits, note);
    }

    /** Hits plus a sentence the model can act on, so an empty result is not a dead end. */
    public record NotebookSearch(List<NotebookHit> notes, String note) {
    }

    private static @Nullable NoteKind parseKind(@Nullable String kind) {
        if (kind == null || kind.isBlank()) {
            return null;
        }
        try {
            return NoteKind.valueOf(kind.strip().toUpperCase(Locale.ROOT).replace(' ', '_'));
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /** A note as the model sees it: the text, its kind, how close it was to the question. */
    public record NotebookHit(String kind, String note, double similarity) {

        static NotebookHit of(ScoredNote hit) {
            return new NotebookHit(hit.note().kind().label(), hit.note().content(),
                    Math.round(hit.similarity() * 100) / 100.0);
        }

    }

}
