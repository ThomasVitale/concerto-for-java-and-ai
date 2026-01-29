package com.thomasvitale.mousike.compositionnote.domain;

import java.util.Objects;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("composition_note")
public record CompositionNoteItem(
        @Id
        UUID id,
        CompositionNote.Type type,
        String content
) {

    public static final String SORT_PROPERTY_TYPE = "type";

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CompositionNoteItem that = (CompositionNoteItem) o;
        return Objects.equals(id.toString(), that.id.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
