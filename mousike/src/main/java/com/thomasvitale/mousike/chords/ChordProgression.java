package com.thomasvitale.mousike.chords;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * A chord progression rendered as MIDI. The only artefact the AI produces, and the composer can always hear it.
 *
 * @param chords the chord symbols, space separated, as given
 * @param voicing the voiced notes per chord, e.g. "D4 F4 A4 | A3 D4 F4"
 * @param midi a Standard MIDI File, type 0
 */
@Table
public record ChordProgression(
        @Id @Nullable UUID id,
        String title,
        String keySignature,
        int tempoBpm,
        int beatsPerChord,
        String chords,
        String voicing,
        byte[] midi,
        @CreatedDate @Nullable Instant createdAt
) {

    public UUID requiredId() {
        if (id == null) {
            throw new IllegalStateException("Chord progression has not been persisted yet");
        }
        return id;
    }

    public List<String> chordList() {
        return Arrays.stream(chords.split("\\s+")).filter(s -> !s.isBlank()).toList();
    }

    public List<String> voicingList() {
        return Arrays.stream(voicing.split("\\s*\\|\\s*")).filter(s -> !s.isBlank()).toList();
    }

    /** Seconds the progression lasts at its tempo. */
    public double seconds() {
        return chordList().size() * beatsPerChord * 60.0 / tempoBpm;
    }

}
