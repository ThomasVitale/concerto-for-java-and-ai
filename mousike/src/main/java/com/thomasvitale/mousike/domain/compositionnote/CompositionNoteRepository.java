package com.thomasvitale.mousike.domain.compositionnote;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

public interface CompositionNoteRepository extends ListCrudRepository<CompositionNote, UUID> {
    List<CompositionNote> findByType(CompositionNote.Type type);
}
