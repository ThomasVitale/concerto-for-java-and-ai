package com.thomasvitale.mousike.notebook;

import java.time.Duration;
import java.util.List;

/**
 * What the decision model said about a note: its choice, and every kind ranked by probability
 * (empty when the provider doesn't report probabilities).
 */
public record NoteTags(
        NoteKind kind,
        List<KindGuess> ranked,
        String taggedBy,
        Duration duration
) {}
