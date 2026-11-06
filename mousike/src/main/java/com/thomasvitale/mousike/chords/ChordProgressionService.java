package com.thomasvitale.mousike.chords;

import com.thomasvitale.mousike.chords.internal.ChordProgressionRepository;
import com.thomasvitale.mousike.chords.internal.MidiProgressionWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Turns chord symbols into a stored, playable MIDI progression. Deterministic music theory, no model involved.
 */
@Service
public class ChordProgressionService {

    public static final int DEFAULT_TEMPO = 130;
    public static final int DEFAULT_BEATS_PER_CHORD = 4;

    private final ChordProgressionRepository progressions;

    ChordProgressionService(ChordProgressionRepository progressions) {
        this.progressions = progressions;
    }

    @Transactional
    public ChordProgression render(String title, String keyText, List<String> chordSymbols, int tempoBpm, int beatsPerChord) {
        if (chordSymbols.isEmpty()) {
            throw new IllegalArgumentException("A progression needs at least one chord");
        }
        Key key = Key.parse(keyText);
        List<Chord> chords = ChordParser.parseAll(chordSymbols, key);
        List<List<Integer>> voiced = Voicing.voice(chords);
        int tempo = Math.clamp(tempoBpm, 30, 220);
        int beats = Math.clamp(beatsPerChord, 1, 16);
        byte[] midi = MidiProgressionWriter.write(title, key, voiced, chordSymbols, tempo, beats);
        boolean flats = key.accidentals() < 0;
        String voicingText = voiced.stream()
                .map(notes -> notes.stream().map(note -> PitchClass.noteName(note, flats)).collect(Collectors.joining(" ")))
                .collect(Collectors.joining(" | "));
        return progressions.save(new ChordProgression(null, title.isBlank() ? String.join(" ", chordSymbols) : title.strip(), key.label(),
                tempo, beats, String.join(" ", chordSymbols), voicingText, midi, null));
    }

    public Optional<ChordProgression> findById(UUID id) {
        return progressions.findById(id);
    }

}
