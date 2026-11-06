package com.thomasvitale.mousike.notebook.internal;

import com.thomasvitale.mousike.notebook.KindCount;
import com.thomasvitale.mousike.notebook.Note;
import com.thomasvitale.mousike.notebook.NoteKind;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

import java.util.List;
import java.util.UUID;

public interface NoteRepository extends ListCrudRepository<Note, UUID>, ListPagingAndSortingRepository<Note, UUID> {

    List<Note> findByContentContainingIgnoreCase(String text, Pageable pageable);

    List<Note> findByKind(NoteKind kind, Pageable pageable);

    List<Note> findByKindAndContentContainingIgnoreCase(NoteKind kind, String text, Pageable pageable);

    @Query("SELECT kind, COUNT(*) AS total FROM note GROUP BY kind")
    List<KindCount> countByKind();

}
