package com.thomasvitale.mousike.chords;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;

import com.thomasvitale.mousike.chords.internal.MidiProgressionWriter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MidiProgressionWriterTests {

    @Test
    void writesAPlayableFileWithMetaEvents() throws Exception {
        Key key = Key.parse("Dm");
        List<String> labels = List.of("Dm", "Bb", "F", "C");
        List<List<Integer>> voiced = Voicing.voice(ChordParser.parseAll(labels, key));
        byte[] midi = MidiProgressionWriter.write("Test progression", key, voiced, labels, 96, 4);

        Sequence sequence = MidiSystem.getSequence(new ByteArrayInputStream(midi));
        assertThat(sequence.getResolution()).isEqualTo(MidiProgressionWriter.PPQ);
        assertThat(sequence.getTracks()).hasSize(1);

        long noteOns = 0;
        boolean tempo = false;
        boolean keySignature = false;
        int markers = 0;
        for (int i = 0; i < sequence.getTracks()[0].size(); i++) {
            MidiEvent event = sequence.getTracks()[0].get(i);
            if (event.getMessage() instanceof ShortMessage sm && sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                noteOns++;
            }
            if (event.getMessage() instanceof MetaMessage mm) {
                tempo |= mm.getType() == 0x51;
                keySignature |= mm.getType() == 0x59;
                if (mm.getType() == 0x06) {
                    markers++;
                }
            }
        }
        assertThat(noteOns).isEqualTo(12);
        assertThat(tempo).isTrue();
        assertThat(keySignature).isTrue();
        assertThat(markers).isEqualTo(4);
        assertThat(sequence.getTickLength()).isEqualTo(4L * 4 * MidiProgressionWriter.PPQ);
    }

}
