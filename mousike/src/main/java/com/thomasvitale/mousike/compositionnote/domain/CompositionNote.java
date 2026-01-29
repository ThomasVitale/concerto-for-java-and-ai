package com.thomasvitale.mousike.compositionnote.domain;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;

public class CompositionNote {
    @Id
    private UUID id;
    @Nullable
    private Type type;
    private String content;

    public CompositionNote() {
    }

    public CompositionNote(UUID id, @Nullable Type type, String content) {
        this.id = id;
        this.type = type;
        this.content = content;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Nullable
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public enum Type {ARRANGEMENT, HARMONY, INSTRUMENT, MELODY, ORCHESTRATION, UNKNOWN}

}
