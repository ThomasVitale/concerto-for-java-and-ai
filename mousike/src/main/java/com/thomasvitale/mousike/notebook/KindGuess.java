package com.thomasvitale.mousike.notebook;

import java.util.Locale;

public record KindGuess(NoteKind kind, double probability) {

    /** For people: "Harmony (48%)". */
    public String describe() {
        return String.format(Locale.ROOT, "%s (%.0f%%)", kind.label(), probability * 100);
    }

}
