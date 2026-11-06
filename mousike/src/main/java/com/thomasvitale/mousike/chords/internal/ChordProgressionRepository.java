package com.thomasvitale.mousike.chords.internal;

import java.util.UUID;

import com.thomasvitale.mousike.chords.ChordProgression;

import org.springframework.data.repository.ListCrudRepository;

public interface ChordProgressionRepository extends ListCrudRepository<ChordProgression, UUID> {}
