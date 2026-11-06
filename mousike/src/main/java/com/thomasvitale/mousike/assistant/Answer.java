package com.thomasvitale.mousike.assistant;

import com.thomasvitale.mousike.chords.ChordProgression;
import com.thomasvitale.mousike.guardrail.GuardrailVerdict;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.List;

/**
 * The assistant's answer with everything the UI needs to show how it came about.
 *
 * @param chordProgressions the chord progressions the model rendered while answering, in the order it made them
 */
public record Answer(
        String markdown,
        List<Citation> citations,
        List<ChordProgression> chordProgressions,
        @Nullable GuardrailVerdict verdict,
        String model,
        Duration duration
) {

    public boolean refused() {
        return verdict != null && !verdict.allowed();
    }

}
