package com.thomasvitale.mousike.notebook.internal;

import com.thomasvitale.mousike.notebook.KindGuess;
import com.thomasvitale.mousike.notebook.NoteKind;
import com.thomasvitale.mousike.notebook.NoteTags;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClassificationPolicyTests {

    private final ClassificationPolicy policy = new ClassificationPolicy(new ClassificationProperties(0.35, 0.10));

    @Test
    void modelDecidesWhenTopKindIsLikelyAndClearlyAhead() {
        assertThat(policy.needsReview(tags(0.54, 0.23))).isFalse();
    }

    @Test
    void personDecidesWhenTopKindIsNotLikelyEnough() {
        assertThat(policy.needsReview(tags(0.30, 0.10))).isTrue();
    }

    @Test
    void personDecidesWhenRunnerUpIsTooClose() {
        assertThat(policy.needsReview(tags(0.45, 0.40))).isTrue();
    }

    @Test
    void modelDecidesWhenNoProbabilitiesAreReported() {
        NoteTags tags = new NoteTags(NoteKind.HARMONY, List.of(), "decision model", Duration.ZERO);

        assertThat(policy.needsReview(tags)).isFalse();
    }

    private static NoteTags tags(double top, double runnerUp) {
        return new NoteTags(NoteKind.INSTRUMENT,
                List.of(new KindGuess(NoteKind.INSTRUMENT, top), new KindGuess(NoteKind.TECHNIQUE, runnerUp)),
                "decision model", Duration.ZERO);
    }

}
