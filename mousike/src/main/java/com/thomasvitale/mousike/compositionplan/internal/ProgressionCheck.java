package com.thomasvitale.mousike.compositionplan.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/**
 * Keeps the planner honest about harmony: a progression comes from the notes only if one note spells out those chords or
 * numerals, in that order. The model's word is not enough, it invents chords however the prompt is phrased.
 */
public final class ProgressionCheck {

    private static final Pattern CHORDS = Pattern.compile("\\(([^)]+)\\)");
    private static final Pattern KEY = Pattern.compile("\\b[A-G][b#♭♯]? (minor|major)\\b");

    private ProgressionCheck() {
    }

    public static boolean isInNotes(List<String> chords, Collection<String> notes) {
        return chords.size() >= 2 && notes.stream().anyMatch(note -> spellsOut(note, chords));
    }

    /** The chords a note spells out in parentheses, like "(Dm Bb F C)". */
    public static List<String> chordsIn(String note) {
        Matcher chords = CHORDS.matcher(note);
        return chords.find() ? List.of(chords.group(1).strip().split("\\s+")) : List.of();
    }

    /**
     * The key of a progression: the one given by the note that spells it out, like "in D minor", or else the model's when it
     * really is a key, or else the first chord's. The model often writes a phrase in this field.
     */
    public static String key(List<String> chords, @Nullable String modelKey, Collection<String> notes) {
        return notes.stream()
                .filter(note -> spellsOut(note, chords))
                .map(KEY::matcher)
                .filter(Matcher::find)
                .map(Matcher::group)
                .findFirst()
                .orElseGet(() -> modelKey != null && KEY.matcher(modelKey.strip()).matches() ? modelKey.strip() : chords.getFirst());
    }

    private static boolean spellsOut(String note, List<String> chords) {
        // Words and chord symbols alike, cut at anything that cannot be part of a symbol: "(Dm Bb F C)", "i-VI-III-VII", "Am to Bb"
        // The key is left out, or the D of "in D minor" would count as a chord
        List<String> words = Arrays.asList(KEY.matcher(note).replaceAll("").split("[^\\p{L}\\p{N}#♯♭°ø+/]+"));
        int next = 0;
        for (String word : words) {
            if (next < chords.size() && word.equals(chords.get(next))) {
                next++;
            }
        }
        return next == chords.size();
    }

}
