package com.thomasvitale.mousike.compositionplan;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * The plan for one scene's music, as the composer reads it: a concept, the instruments to load, the harmonic ideas (each
 * with a playable progression when the chords parsed) and what happens at every time marker.
 *
 * @param gatherDuration time of the agentic gathering step
 * @param structureDuration time of the structuring step
 */
public record CompositionSheet(
        String concept,
        List<PaletteEntry> palette,
        List<HarmonicIdea> harmony,
        List<OrchestrationStep> arc,
        Duration gatherDuration,
        Duration structureDuration
) {

    public CompositionSheet {
        palette = List.copyOf(palette);
        harmony = List.copyOf(harmony);
        arc = List.copyOf(arc);
    }

    /**
     * @param role what the instrument does in the music, e.g. "low pulse", "theme"
     * @param preset the exact Kontakt preset name to load, when the library had one
     * @param library the product the preset belongs to, as Kontakt's Browser names its Library tile
     * @param preview the library's sample clip for the preset, when the library found it and its library ships one
     */
    public record PaletteEntry(String role, String instrument, @Nullable String preset, @Nullable String library, String why,
            @Nullable URI preview) {
    }

    /**
     * @param progressionId the rendered progression, absent when the chords could not be parsed
     * @param fromNotes whether one of the composer's notes spells it out, or the model suggested it
     */
    public record HarmonicIdea(String name, String key, List<String> chords, String why, @Nullable UUID progressionId, boolean fromNotes) {
        public HarmonicIdea {
            chords = List.copyOf(chords);
        }
    }

    public record OrchestrationStep(String timeMarker, String instruction, List<String> instruments) {
        public OrchestrationStep {
            instruments = List.copyOf(instruments);
        }
    }

}
