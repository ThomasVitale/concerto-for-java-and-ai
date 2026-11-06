package com.thomasvitale.mousike.chords;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A key: tonic pitch class and mode. Parses "Dm", "D minor", "F# major", "Bb", "Eb min".
 */
public record Key(int tonic, Mode mode) {

    public enum Mode { MAJOR, MINOR }

    private static final List<Integer> MAJOR_DEGREES = List.of(0, 2, 4, 5, 7, 9, 11);
    private static final List<Integer> MINOR_DEGREES = List.of(0, 2, 3, 5, 7, 8, 10);

    public static Key parse(String text) {
        String trimmed = text.strip();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Key must not be blank");
        }
        int tonic = PitchClass.parse(trimmed);
        String rest = trimmed.substring(PitchClass.length(trimmed)).strip().toLowerCase(Locale.ROOT);
        Mode mode = rest.startsWith("m") && !rest.startsWith("maj") ? Mode.MINOR : Mode.MAJOR;
        return new Key(tonic, mode);
    }

    /** Pitch class of a zero-based scale degree (0 = tonic), diatonic to the mode. */
    public int degreePitchClass(int degree) {
        List<Integer> degrees = mode == Mode.MAJOR ? MAJOR_DEGREES : MINOR_DEGREES;
        return (tonic + degrees.get(Math.floorMod(degree, 7))) % 12;
    }

    /**
     * The triad built on a zero-based scale degree, as the harmony charts name it: in major I ii iii IV V vi
     * vii°, in minor i ii° III iv V VI VII, with the dominant major as a cadence wants it.
     */
    public Chord diatonic(String label, int degree) {
        int root = degreePitchClass(degree);
        List<Integer> intervals = new ArrayList<>(List.of(0));
        for (int third = 2; third <= 4; third += 2) {
            int interval = Math.floorMod(degreePitchClass(degree + third) - root, 12);
            boolean leadingTone = mode == Mode.MINOR && degree == 4 && third == 2;
            intervals.add(leadingTone ? interval + 1 : interval);
        }
        return new Chord(label, root, intervals, null);
    }

    public String label() {
        return PitchClass.name(tonic, mode == Mode.MINOR) + (mode == Mode.MINOR ? " minor" : " major");
    }

    /** Sharps (positive) or flats (negative) for the MIDI key signature meta event. */
    public int accidentals() {
        int majorTonic = mode == Mode.MAJOR ? tonic : (tonic + 3) % 12;
        return switch (majorTonic) {
            case 0 -> 0; case 7 -> 1; case 2 -> 2; case 9 -> 3; case 4 -> 4; case 11 -> 5; case 6 -> 6;
            case 5 -> -1; case 10 -> -2; case 3 -> -3; case 8 -> -4; case 1 -> -5;
            default -> 0;
        };
    }

}
