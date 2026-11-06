package com.thomasvitale.mousike.compositionplan.internal;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProgressionCheckTests {

    private static final List<String> NOTES = List.of(
            "For epic or action scenes, the progression i-VI-III-VII (Dm Bb F C) keeps moving and never fully resolves.",
            "For mystery and noir, I alternate i and bII (Am to Bb) over a low pedal.",
            "For romance, try Imaj7-vi7-ii9-V7sus4 (Cmaj7 Am7 Dm9 G7sus4). The suspended dominant delays the resolution.");

    @Test
    void chordsANoteSpellsOutStand() {
        assertThat(ProgressionCheck.isInNotes(List.of("Dm", "Bb", "F", "C"), NOTES)).isTrue();
        assertThat(ProgressionCheck.isInNotes(List.of("Cmaj7", "Am7", "Dm9", "G7sus4"), NOTES)).isTrue();
        assertThat(ProgressionCheck.isInNotes(List.of("Am", "Bb"), NOTES)).isTrue();
    }

    @Test
    void theKeyComesFromTheNoteThatSpellsOutTheChords() {
        List<String> notes = List.of("For a noir funeral march in G minor, the progression iv-V-i-V (Cm D Gm D) keeps circling.",
                "For melancholy in D minor, play i-VI-III-VII (Dm Bb F C) slowly.");

        assertThat(ProgressionCheck.key(List.of("Cm", "D", "Gm", "D"), "minor key funeral march", notes)).isEqualTo("G minor");
        assertThat(ProgressionCheck.key(List.of("Dm", "Bb", "F", "C"), "A minor", notes)).isEqualTo("D minor");
    }

    @Test
    void withoutANoteTheKeyIsTheModelsWhenItIsOneOrElseTheFirstChords() {
        List<String> notes = List.of("A progression (Am F C G) with no key.");

        assertThat(ProgressionCheck.key(List.of("Am", "F", "C", "G"), "C major", notes)).isEqualTo("C major");
        assertThat(ProgressionCheck.key(List.of("Em", "C", "G", "D"), "a dark mood", notes)).isEqualTo("Em");
    }

    @Test
    void theNotesKeyDoesNotCountAsAChord() {
        List<String> notes = List.of("For melancholy in D minor, play i-VI-III-VII (Dm Bb F C) slowly.");

        assertThat(ProgressionCheck.isInNotes(List.of("D", "Bb", "F", "C"), notes)).isFalse();
        assertThat(ProgressionCheck.isInNotes(List.of("Dm", "Bb", "F", "C"), notes)).isTrue();
    }

    @Test
    void chordsComeFromTheNotesParentheses() {
        assertThat(ProgressionCheck.chordsIn("For a noir march, the progression iv-V-i-V (Cm D Gm D) keeps circling.")).containsExactly("Cm", "D", "Gm", "D");
        assertThat(ProgressionCheck.chordsIn("A note without chords.")).isEmpty();
    }

    @Test
    void numeralsANoteSpellsOutStand() {
        assertThat(ProgressionCheck.isInNotes(List.of("i", "VI", "III", "VII"), NOTES)).isTrue();
        assertThat(ProgressionCheck.isInNotes(List.of("Imaj7", "vi7", "ii9", "V7sus4"), NOTES)).isTrue();
    }

    @Test
    void inventedChordsDoNotStand() {
        assertThat(ProgressionCheck.isInNotes(List.of("F#m7", "Bmaj7", "A6"), NOTES)).isFalse();
        assertThat(ProgressionCheck.isInNotes(List.of("D", "F#", "A", "C#"), NOTES)).isFalse();
    }

    @Test
    void chordsOutOfOrderOrAcrossNotesDoNotStand() {
        assertThat(ProgressionCheck.isInNotes(List.of("C", "F", "Bb", "Dm"), NOTES)).isFalse();
        assertThat(ProgressionCheck.isInNotes(List.of("Dm", "Am7"), NOTES)).isFalse();
    }

    @Test
    void aSingleChordOrAMalformedOneDoesNotStand() {
        assertThat(ProgressionCheck.isInNotes(List.of("G7sus4"), NOTES)).isFalse();
        assertThat(ProgressionCheck.isInNotes(List.of("Dm-Bb-F-C"), NOTES)).isFalse();
    }

}
