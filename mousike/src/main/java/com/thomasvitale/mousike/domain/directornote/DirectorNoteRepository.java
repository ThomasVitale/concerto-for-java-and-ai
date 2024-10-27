package com.thomasvitale.mousike.domain.directornote;

import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

public interface DirectorNoteRepository extends ListCrudRepository<DirectorNote, UUID> {
}

