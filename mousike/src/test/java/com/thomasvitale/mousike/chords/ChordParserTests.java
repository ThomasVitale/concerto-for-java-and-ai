package com.thomasvitale.mousike.chords;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChordParserTests {

    private final Key dMinor = Key.parse("Dm");
    private final Key cMajor = Key.parse("C major");

    @Test
    void parsesAbsoluteChords() {
        assertThat(ChordParser.parse("Dm", dMinor).pitchClasses()).containsExactly(2, 5, 9);
        assertThat(ChordParser.parse("Bbmaj7", dMinor).pitchClasses()).containsExactly(10, 2, 5, 9);
        assertThat(ChordParser.parse("F#dim", dMinor).pitchClasses()).containsExactly(6, 9, 0);
        assertThat(ChordParser.parse("Gsus4", dMinor).pitchClasses()).containsExactly(7, 0, 2);
        assertThat(ChordParser.parse("C/E", cMajor).bass()).isEqualTo(4);
    }

    @Test
    void parsesRomanNumeralsInMinor() {
        List<Chord> chords = ChordParser.parseAll(List.of("i", "VI", "III", "VII"), dMinor);
        assertThat(chords).extracting(Chord::root).containsExactly(2, 10, 5, 0);
        assertThat(chords.get(0).intervals()).containsExactly(0, 3, 7);
        assertThat(chords.get(1).intervals()).containsExactly(0, 4, 7);
    }

    @Test
    void parsesRomanNumeralsInMajorWithAccidentalsAndQualities() {
        assertThat(ChordParser.parse("bVI", cMajor).root()).isEqualTo(8);
        assertThat(ChordParser.parse("bII", cMajor).root()).isEqualTo(1);
        assertThat(ChordParser.parse("V7", cMajor).intervals()).containsExactly(0, 4, 7, 10);
        assertThat(ChordParser.parse("ii7", cMajor).intervals()).containsExactly(0, 3, 7, 10);
        assertThat(ChordParser.parse("vii°", cMajor).intervals()).containsExactly(0, 3, 6);
        assertThat(ChordParser.parse("IVmaj7", cMajor).intervals()).containsExactly(0, 4, 7, 11);
    }

    @Test
    void rejectsNonsense() {
        assertThatThrownBy(() -> ChordParser.parse("Hm", cMajor)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ChordParser.parse("Cxyz", cMajor)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void keysParseAndCarrySignatures() {
        assertThat(Key.parse("F# major").accidentals()).isEqualTo(6);
        assertThat(Key.parse("Bb").accidentals()).isEqualTo(-2);
        assertThat(Key.parse("Am").accidentals()).isZero();
        assertThat(Key.parse("Dm").label()).isEqualTo("D minor");
    }


    @Test
    void alteredTensionsSitOnTopOfTheKnownQuality() {
        Key dMinor = Key.parse("D minor");
        assertThat(ChordParser.parse("Bm7b9", dMinor).intervals()).containsExactly(0, 3, 7, 10, 13);
        assertThat(ChordParser.parse("G7#11", dMinor).intervals()).containsExactly(0, 4, 7, 10, 18);
        assertThat(ChordParser.parse("C13", dMinor).intervals()).containsExactly(0, 4, 7, 10, 21);
        assertThat(ChordParser.parse("F7b5", dMinor).intervals()).containsExactly(0, 4, 10, 6);
        assertThatThrownBy(() -> ChordParser.parse("Dxyz9", dMinor)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bareNumeralsAreDegreesOfTheKey() {
        Key cMinor = Key.parse("C minor");
        assertThat(ChordParser.parseAll(List.of("ii", "V", "I"), cMinor)).extracting(chord -> chord.symbol(cMinor))
                .containsExactly("Ddim", "G", "Cm");
        assertThat(ChordParser.parseAll(List.of("ii", "V", "I"), cMajor)).extracting(chord -> chord.symbol(cMajor))
                .containsExactly("Dm", "G", "C");
    }

    @Test
    void theSameNumeralsMoveToAnotherKey() {
        Key gMinor = Key.parse("G minor");
        assertThat(ChordParser.parseAll(List.of("i", "VI", "III", "VII"), dMinor)).extracting(chord -> chord.symbol(dMinor))
                .containsExactly("Dm", "Bb", "F", "C");
        assertThat(ChordParser.parseAll(List.of("i", "VI", "III", "VII"), gMinor)).extracting(chord -> chord.symbol(gMinor))
                .containsExactly("Gm", "Eb", "Bb", "F");
    }

    @Test
    void numeralsFromTheNotebookKeepTheirExtensions() {
        assertThat(ChordParser.parseAll(List.of("Imaj7", "vi7", "ii9", "V7sus4"), cMajor)).extracting(chord -> chord.symbol(cMajor))
                .containsExactly("Cmaj7", "Am7", "Dm9", "G7sus4");
        assertThat(ChordParser.parseAll(List.of("i", "bII"), Key.parse("A minor"))).extracting(chord -> chord.symbol(Key.parse("A minor")))
                .containsExactly("Am", "Bb");
    }

}
