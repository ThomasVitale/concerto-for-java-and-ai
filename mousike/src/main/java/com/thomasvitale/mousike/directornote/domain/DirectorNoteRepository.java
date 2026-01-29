package com.thomasvitale.mousike.directornote.domain;

import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

public interface DirectorNoteRepository extends ListCrudRepository<DirectorNote, UUID> {
}

