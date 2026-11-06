package com.thomasvitale.mousike.chords;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VoicingTests {

    @Test
    void firstChordSitsAroundMiddleCInRootPosition() {
        List<List<Integer>> voiced = Voicing.voice(ChordParser.parseAll(List.of("Dm"), Key.parse("Dm")));
        assertThat(voiced.getFirst()).containsExactly(62, 65, 69);
    }

    @Test
    void consecutiveChordsMoveLittleAndStayInRange() {
        List<List<Integer>> voiced = Voicing.voice(ChordParser.parseAll(List.of("i", "VI", "III", "VII"), Key.parse("Dm")));
        assertThat(voiced).hasSize(4);
        for (int i = 1; i < voiced.size(); i++) {
            int previousCentre = voiced.get(i - 1).stream().mapToInt(Integer::intValue).sum() / voiced.get(i - 1).size();
            int centre = voiced.get(i).stream().mapToInt(Integer::intValue).sum() / voiced.get(i).size();
            assertThat(Math.abs(centre - previousCentre)).isLessThanOrEqualTo(7);
            assertThat(voiced.get(i)).allSatisfy(note -> assertThat(note).isBetween(Voicing.LOWEST, Voicing.HIGHEST));
        }
    }

    @Test
    void slashBassBecomesLowestNote() {
        List<List<Integer>> voiced = Voicing.voice(ChordParser.parseAll(List.of("C", "C/E"), Key.parse("C")));
        assertThat(voiced.get(1).getFirst() % 12).isEqualTo(4);
    }

}
