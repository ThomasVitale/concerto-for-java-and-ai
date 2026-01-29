package com.thomasvitale.mousike.compositionnote.domain;

import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

public interface CompositionNoteRepository extends ListCrudRepository<CompositionNote, UUID> {
}
