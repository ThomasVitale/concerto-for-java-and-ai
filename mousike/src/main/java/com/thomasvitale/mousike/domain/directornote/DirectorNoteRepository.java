package com.thomasvitale.mousike.domain.directornote;

import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface DirectorNoteRepository extends ListCrudRepository<DirectorNote, UUID> {
}

