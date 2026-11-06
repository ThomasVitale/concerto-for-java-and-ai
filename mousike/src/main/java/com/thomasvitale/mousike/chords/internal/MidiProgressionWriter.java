package com.thomasvitale.mousike.chords.internal;

import com.thomasvitale.mousike.chords.Key;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Writes block chords to a Standard MIDI File with javax.sound.midi. The file carries tempo, time signature,
 * and key signature meta events so that a DAW such as Logic Pro opens it as a correctly configured project seed.
 */
public final class MidiProgressionWriter {

    public static final int PPQ = 480;
    static final int PIANO = 0;
    static final int VELOCITY = 84;

    private MidiProgressionWriter() {}

    public static byte[] write(String title, Key key, List<List<Integer>> voicedChords, List<String> labels, int tempoBpm,
            int beatsPerChord) {
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, PPQ);
            Track track = sequence.createTrack();
            track.add(new MidiEvent(trackName(title), 0));
            track.add(new MidiEvent(tempo(tempoBpm), 0));
            track.add(new MidiEvent(timeSignature(4, 4), 0));
            track.add(new MidiEvent(keySignature(key), 0));
            track.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, 0, PIANO, 0), 0));

            long tick = 0;
            long chordTicks = (long) beatsPerChord * PPQ;
            for (int i = 0; i < voicedChords.size(); i++) {
                String label = i < labels.size() ? labels.get(i) : "";
                if (!label.isBlank()) {
                    track.add(new MidiEvent(marker(label), tick));
                }
                for (int note : voicedChords.get(i)) {
                    track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, note, VELOCITY), tick));
                    track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, note, 0), tick + chordTicks - PPQ / 16));
                }
                tick += chordTicks;
            }
            track.add(new MidiEvent(new MetaMessage(0x2F, new byte[0], 0), tick));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MidiSystem.write(sequence, 0, out);
            return out.toByteArray();
        } catch (InvalidMidiDataException | IOException ex) {
            throw new IllegalStateException("Could not write MIDI progression", ex);
        }
    }

    static MetaMessage tempo(int bpm) throws InvalidMidiDataException {
        int microsPerQuarter = 60_000_000 / Math.max(20, Math.min(300, bpm));
        byte[] data = { (byte) (microsPerQuarter >> 16), (byte) (microsPerQuarter >> 8), (byte) microsPerQuarter };
        return new MetaMessage(0x51, data, 3);
    }

    static MetaMessage timeSignature(int numerator, int denominator) throws InvalidMidiDataException {
        int denominatorPower = (int) (Math.log(denominator) / Math.log(2));
        byte[] data = { (byte) numerator, (byte) denominatorPower, 24, 8 };
        return new MetaMessage(0x58, data, 4);
    }

    static MetaMessage keySignature(Key key) throws InvalidMidiDataException {
        byte[] data = { (byte) key.accidentals(), (byte) (key.mode() == Key.Mode.MINOR ? 1 : 0) };
        return new MetaMessage(0x59, data, 2);
    }

    static MetaMessage trackName(String name) throws InvalidMidiDataException {
        byte[] data = name.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return new MetaMessage(0x03, data, data.length);
    }

    static MetaMessage marker(String text) throws InvalidMidiDataException {
        byte[] data = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return new MetaMessage(0x06, data, data.length);
    }

}
