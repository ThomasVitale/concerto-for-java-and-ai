package com.thomasvitale.mousike.ai.observations;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Spring AI observations.
 */
@ConfigurationProperties(ObservationsProperties.CONFIG_PREFIX)
public class ObservationsProperties {

    public static final String CONFIG_PREFIX = "mousike.spring.ai.observations";

    /**
     * Whether to enable observations for Spring AI.
     */
    private boolean enabled = true;

    /**
     * Whether to include the prompt content in the observations.
     */
    private boolean includePrompt = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isIncludePrompt() {
        return includePrompt;
    }

    public void setIncludePrompt(boolean includePrompt) {
        this.includePrompt = includePrompt;
    }

}
