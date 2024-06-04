package com.thomasvitale.mousike.ai.observations.chat;

import io.micrometer.common.KeyValues;

/**
 * Default {@link ChatObservationConvention} implementation.
 */
public class DefaultChatObservationConvention implements ChatObservationConvention {

    @Override
    public String getName() {
        return ChatObservation.CHAT_OBSERVATION.getName();
    }

    @Override
    public String getContextualName(ChatObservationContext context) {
        return ChatObservation.CHAT_OBSERVATION.getContextualName();
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(ChatObservationContext context) {
        var keyValues = KeyValues.empty();
        keyValues = addTokenUsage(keyValues, context);
        keyValues = addFinishReason(keyValues, context);
        return keyValues;
    }

    private KeyValues addTokenUsage(KeyValues keyValues, ChatObservationContext context) {
        if (context.getUsage() != null) {
            if (context.getUsage().getPromptTokens() != null) {
                keyValues = keyValues.and(ChatObservation.ChatHighCardinalityTags.USAGE_TOKENS_INPUT.withValue(String.valueOf(context.getUsage().getPromptTokens())));
            }
            if (context.getUsage().getGenerationTokens() != null) {
                keyValues = keyValues.and(ChatObservation.ChatHighCardinalityTags.USAGE_TOKENS_OUTPUT.withValue(String.valueOf(context.getUsage().getGenerationTokens())));
            }
            if (context.getUsage().getTotalTokens() != null) {
                keyValues = keyValues.and(ChatObservation.ChatHighCardinalityTags.USAGE_TOKENS_TOTAL.withValue(String.valueOf(context.getUsage().getTotalTokens())));
            }
        }
        return keyValues;
    }

    private KeyValues addFinishReason(KeyValues keyValues, ChatObservationContext context) {
        if (context.getFinishReason() != null) {
            keyValues = keyValues.and(ChatObservation.ChatHighCardinalityTags.FINISH_REASON.withValue(String.valueOf(context.getFinishReason()).toLowerCase()));
        }
        return keyValues;
    }

}
