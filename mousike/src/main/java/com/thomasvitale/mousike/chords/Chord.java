package com.thomasvitale.mousike.chords;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A chord as pitch classes: a root, intervals above it in semitones, an optional bass for slash chords.
 */
public record Chord(String label, int root, List<Integer> intervals, @Nullable Integer bass) {

    public Chord {
        intervals = List.copyOf(intervals);
    }

    /**
     * The absolute chord symbol, spelled for the key: "F#m7", "Bb/D".
     */
    public String symbol(Key key) {
        boolean flats = key.accidentals() < 0 || label.startsWith("b");
        String symbol = PitchClass.name(root, flats) + ChordParser.quality(intervals);
        return bass == null ? symbol : symbol + "/" + PitchClass.name(bass, flats);
    }

    /**
     * Pitch classes of the chord tones, root first.
     */
    public List<Integer> pitchClasses() {
        return intervals.stream().map(interval -> (root + interval) % 12).toList();
    }

}
