package com.thomasvitale.mousike.ai.observations.chat;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;

/**
 * An {@link ObservationFilter} to populate chat prompt specific information.
 */
public class ChatPromptObservationFilter implements ObservationFilter {

    @Override
    public Observation.Context map(Observation.Context context) {
        if (!(context instanceof ChatObservationContext chatObservationContext)) {
            return context;
        }

        if (chatObservationContext.getPrompt() == null) {
            return chatObservationContext;
        }

        chatObservationContext.addHighCardinalityKeyValue(
            ChatObservation.ChatHighCardinalityTags.PROMPT.withValue(chatObservationContext.getPrompt().toString())
        );

        return chatObservationContext;
    }

}
