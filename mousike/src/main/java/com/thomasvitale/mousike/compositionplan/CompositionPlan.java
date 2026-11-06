package com.thomasvitale.mousike.compositionplan;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A saved composition plan for a scene. The sheet itself is one JSON document: it is read as a whole, never queried.
 *
 * @param model the chat model that planned it
 */
@Table("composition_plan")
public record CompositionPlan(
        @Id @Nullable UUID id,
        UUID sceneId,
        @Nullable String model,
        CompositionSheet plan,
        @CreatedDate @Nullable Instant createdAt
) {

    public UUID requiredId() {
        if (id == null) {
            throw new IllegalStateException("Composition plan has not been persisted yet");
        }
        return id;
    }

}
