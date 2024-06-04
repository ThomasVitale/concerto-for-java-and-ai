package com.thomasvitale.mousike.ai.observations.chat;

import io.micrometer.observation.Observation;

import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Observation context for chat model interactions.
 */
public class ChatObservationContext extends Observation.Context {

    @Nullable
    private String finishReason;
    @Nullable
    private Prompt prompt;
    @Nullable
    private Usage usage;

    @Nullable
    public String getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(String finishReason) {
        Assert.hasText(finishReason, "finishReason cannot be null or empty");
        this.finishReason = finishReason;
    }

    @Nullable
    public Prompt getPrompt() {
        return prompt;
    }

    public void setPrompt(Prompt prompt) {
        Assert.notNull(prompt, "prompt cannot be null");
        this.prompt = prompt;
    }

    @Nullable
    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        Assert.notNull(usage, "usage cannot be null");
        this.usage = usage;
    }

}
