package com.thomasvitale.mousike.notebook;

import java.util.List;

/**
 * The outcome of classifying a note with the decision model: either the model was sure enough and the note is saved,
 * or it wasn't, and a person has to choose the kind. Nothing is saved in that case.
 */
public sealed interface Classification {

    record Classified(Note note) implements Classification {}

    /** The model's best guesses, most likely first. */
    record NeedsReview(List<KindGuess> candidates) implements Classification {}

}
