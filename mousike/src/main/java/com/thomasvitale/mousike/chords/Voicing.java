package com.thomasvitale.mousike.chords;

import java.util.ArrayList;
import java.util.List;

/**
 * Block chords, voiced so consecutive chords move as little as possible. Deterministic, nothing creative:
 * the first chord sits around middle C in root position; every next chord takes the inversion closest to the
 * previous one. A slash bass, when given, becomes the lowest note.
 */
public final class Voicing {

    static final int LOWEST = 48;   // C3
    static final int HIGHEST = 79;  // G5

    private Voicing() {
    }

    /** MIDI note numbers for each chord, lowest first. */
    public static List<List<Integer>> voice(List<Chord> chords) {
        List<List<Integer>> result = new ArrayList<>();
        List<Integer> previous = List.of();
        for (Chord chord : chords) {
            List<Integer> voiced = previous.isEmpty() ? rootPosition(chord) : closest(chord, previous);
            if (chord.bass() != null) {
                voiced = withBass(voiced, chord.bass());
            }
            result.add(voiced);
            previous = voiced;
        }
        return result;
    }

    private static List<Integer> rootPosition(Chord chord) {
        int root = 60 + chord.root();   // C4 .. B4
        if (root > 65) {
            root -= 12;                 // keep the root between F#3 and F4
        }
        List<Integer> notes = new ArrayList<>();
        for (int interval : chord.intervals()) {
            notes.add(root + interval);
        }
        return notes;
    }

    private static List<Integer> closest(Chord chord, List<Integer> previous) {
        List<Integer> best = List.of();
        int bestCost = Integer.MAX_VALUE;
        List<Integer> pitchClasses = chord.pitchClasses();
        int size = pitchClasses.size();
        // Try every inversion at every reasonable octave and keep the one with the least total movement
        for (int inversion = 0; inversion < size; inversion++) {
            for (int octave = 3; octave <= 5; octave++) {
                List<Integer> candidate = new ArrayList<>();
                int bottom = (octave + 1) * 12 + pitchClasses.get(inversion);
                int last = bottom;
                candidate.add(bottom);
                for (int step = 1; step < size; step++) {
                    int pitchClass = pitchClasses.get((inversion + step) % size);
                    int note = last + Math.floorMod(pitchClass - last, 12);
                    if (note == last) {
                        note += 12;
                    }
                    candidate.add(note);
                    last = note;
                }
                if (candidate.getFirst() < LOWEST || candidate.getLast() > HIGHEST) {
                    continue;
                }
                int cost = movement(previous, candidate);
                if (cost < bestCost) {
                    bestCost = cost;
                    best = candidate;
                }
            }
        }
        return best.isEmpty() ? rootPosition(chord) : best;
    }

    private static int movement(List<Integer> from, List<Integer> to) {
        int cost = 0;
        for (int note : to) {
            int nearest = Integer.MAX_VALUE;
            for (int previous : from) {
                nearest = Math.min(nearest, Math.abs(note - previous));
            }
            cost += nearest;
        }
        // Penalise drifting away from the previous centre
        cost += Math.abs(centre(from) - centre(to));
        return cost;
    }

    private static int centre(List<Integer> notes) {
        return notes.stream().mapToInt(Integer::intValue).sum() / Math.max(1, notes.size());
    }

    private static List<Integer> withBass(List<Integer> voiced, int bassPitchClass) {
        int lowest = voiced.getFirst();
        int bass = lowest - Math.floorMod(lowest - bassPitchClass, 12);
        if (bass == lowest) {
            return voiced;
        }
        List<Integer> notes = new ArrayList<>();
        notes.add(bass);
        notes.addAll(voiced.stream().filter(n -> n != bass).toList());
        return notes;
    }

}
