package com.thomasvitale.mousike.chords;

import java.util.List;

/**
 * Note names to pitch classes (C = 0) and back.
 */
final class PitchClass {

    private static final List<String> SHARP_NAMES = List.of("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
    private static final List<String> FLAT_NAMES = List.of("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B");

    private PitchClass() {
    }

    /** Parses the note name at the start of the text, e.g. "F#" in "F#m7". */
    static int parse(String text) {
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Missing note name");
        }
        char letter = Character.toUpperCase(text.charAt(0));
        int base = switch (letter) {
            case 'C' -> 0; case 'D' -> 2; case 'E' -> 4; case 'F' -> 5; case 'G' -> 7; case 'A' -> 9; case 'B' -> 11;
            default -> throw new IllegalArgumentException("Not a note name: " + text);
        };
        int index = 1;
        while (index < text.length()) {
            char c = text.charAt(index);
            if (c == '#' || c == '♯') {
                base++;
            }
            else if (c == 'b' || c == '♭') {
                base--;
            }
            else {
                break;
            }
            index++;
        }
        return Math.floorMod(base, 12);
    }

    /** Length of the note name prefix (letter plus accidentals). */
    static int length(String text) {
        int index = 1;
        while (index < text.length() && "#♯b♭".indexOf(text.charAt(index)) >= 0) {
            index++;
        }
        return index;
    }

    static String name(int pitchClass, boolean preferFlats) {
        return (preferFlats ? FLAT_NAMES : SHARP_NAMES).get(Math.floorMod(pitchClass, 12));
    }

    /** "C4" style name for a MIDI note number (60 = C4), spelled with sharps. */
    static String noteName(int midi) {
        return noteName(midi, false);
    }

    /** "C4" style name for a MIDI note number (60 = C4), spelled with flats when the key asks for them. */
    static String noteName(int midi, boolean preferFlats) {
        return name(midi, preferFlats) + (midi / 12 - 1);
    }

}
