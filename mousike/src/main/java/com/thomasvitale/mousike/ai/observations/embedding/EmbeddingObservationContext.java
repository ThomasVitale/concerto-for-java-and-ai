package com.thomasvitale.mousike.ai.observations.embedding;

import io.micrometer.observation.Observation;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Observation context for embedding model interactions.
 */
public class EmbeddingObservationContext extends Observation.Context {

    @Nullable
    private Usage usage;

    @Nullable
    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        Assert.notNull(usage, "usage cannot be null");
        this.usage = usage;
    }

}
