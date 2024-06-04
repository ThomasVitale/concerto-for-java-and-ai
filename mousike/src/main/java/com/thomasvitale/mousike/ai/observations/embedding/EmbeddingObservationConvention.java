package com.thomasvitale.mousike.ai.observations.embedding;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

/**
 * {@link ObservationConvention} for {@link EmbeddingObservationContext}.
 */
public interface EmbeddingObservationConvention extends ObservationConvention<EmbeddingObservationContext> {

    @Override
    default boolean supportsContext(Observation.Context context) {
        return context instanceof EmbeddingObservationContext;
    }

}
