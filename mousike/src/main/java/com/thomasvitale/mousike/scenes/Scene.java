package com.thomasvitale.mousike.scenes;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Table
public record Scene(
        @Id @Nullable UUID id,
        @Nullable String production,
        String title,
        String description,
        Medium medium,
        @Nullable String genre,
        @Nullable String transcript,
        @MappedCollection(idColumn = "scene", keyColumn = "scene_key") List<SceneMarker> markers,
        @CreatedDate @Nullable Instant createdAt
) {

    public Scene {
        markers = List.copyOf(markers);
    }

    public static Scene create(@Nullable String production, String title, String description, Medium medium,
            @Nullable String genre, @Nullable String transcript, List<SceneMarker> markers) {
        return new Scene(null, blankToNull(production), title.strip(), description.strip(), medium, blankToNull(genre),
                transcript, markers, null);
    }

    public UUID requiredId() {
        if (id == null) {
            throw new IllegalStateException("Scene has not been persisted yet");
        }
        return id;
    }

    private static @Nullable String blankToNull(@Nullable String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

}
