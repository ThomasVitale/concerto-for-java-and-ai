package com.thomasvitale.mousike.guardrail;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Thresholds for the decision-model guardrail.
 *
 * @param onTopicThreshold minimum probability that a request is about music, scoring or this app
 * @param injectionThreshold probability above which a request counts as an attempt to hijack the assistant
 * @param creativeWorkThreshold probability above which a request counts as asking for creative work
 */
@ConfigurationProperties(prefix = "mousike.guardrail")
public record GuardrailProperties(
        @DefaultValue("0.5") double onTopicThreshold,
        @DefaultValue("0.7") double injectionThreshold,
        @DefaultValue("0.85") double creativeWorkThreshold) {
}
