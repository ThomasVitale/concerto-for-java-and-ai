
package com.thomasvitale.mousike.compositionnote.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CompositionNoteItemRepository extends PagingAndSortingRepository<CompositionNoteItem, UUID> {
    List<CompositionNoteItem> findAllById(Iterable<UUID> ids);
    Optional<CompositionNoteItem> findById(UUID id);
    Slice<CompositionNoteItem> findByContentIgnoreCaseContaining(String keyword, Pageable pageable);
}
