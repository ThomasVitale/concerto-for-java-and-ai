package com.thomasvitale.mousike.ai.observations.embedding;

import io.micrometer.common.KeyValues;

/**
 * Default {@link EmbeddingObservationConvention} implementation.
 */
public final class DefaultEmbeddingObservationConvention implements EmbeddingObservationConvention {

    @Override
    public String getName() {
        return EmbeddingObservation.EMBEDDING_OBSERVATION.getName();
    }

    @Override
    public String getContextualName(EmbeddingObservationContext context) {
        return EmbeddingObservation.EMBEDDING_OBSERVATION.getContextualName();
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(EmbeddingObservationContext context) {
        return addTokenUsage(KeyValues.empty(), context);
    }

    private KeyValues addTokenUsage(KeyValues keyValues, EmbeddingObservationContext context) {
        if (context.getUsage() != null) {
            if (context.getUsage().getPromptTokens() != null) {
                keyValues = keyValues.and(EmbeddingObservation.EmbeddingHighCardinalityTags.USAGE_TOKENS_INPUT.withValue(String.valueOf(context.getUsage().getPromptTokens())));
            }
            if (context.getUsage().getGenerationTokens() != null) {
                keyValues = keyValues.and(EmbeddingObservation.EmbeddingHighCardinalityTags.USAGE_TOKENS_OUTPUT.withValue(String.valueOf(context.getUsage().getGenerationTokens())));
            }
            if (context.getUsage().getTotalTokens() != null) {
                keyValues = keyValues.and(EmbeddingObservation.EmbeddingHighCardinalityTags.USAGE_TOKENS_TOTAL.withValue(String.valueOf(context.getUsage().getTotalTokens())));
            }
        }
        return keyValues;
    }

}
