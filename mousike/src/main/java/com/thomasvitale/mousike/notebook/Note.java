package com.thomasvitale.mousike.notebook;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Table
public record Note(
        @Id @Nullable UUID id,
        String content,
        NoteKind kind,
        @Nullable String taggedBy,
        @Nullable Duration taggingDuration,
        @CreatedDate @Nullable Instant createdAt,
        @LastModifiedDate @Nullable Instant updatedAt
) {

    static Note create(String content, NoteTags tags) {
        return new Note(null, content, tags.kind(), tags.taggedBy(), tags.duration(), null, null);
    }

    static Note create(String content, NoteKind kind) {
        return new Note(null, content, kind, null, null, null, null);
    }

    Note revised(String content, NoteTags tags) {
        return new Note(id, content, tags.kind(), tags.taggedBy(), tags.duration(), createdAt, updatedAt);
    }

    Note revised(String content, NoteKind kind) {
        return new Note(id, content, kind, null, null, createdAt, updatedAt);
    }

    public UUID requiredId() {
        if (id == null) {
            throw new IllegalStateException("Note has not been persisted yet");
        }
        return id;
    }

    public String indexText() {
        return kind.label() + ". " + content;
    }

}
