package com.thomasvitale.mousike.notebook.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * When the decision model's choice of kind stands on its own.
 *
 * @param minProbability minimum probability of the top kind
 * @param minMargin minimum lead of the top kind over the runner-up; closer than this, a person decides
 */
@ConfigurationProperties(prefix = "mousike.classification")
public record ClassificationProperties(
        @DefaultValue("0.35") double minProbability,
        @DefaultValue("0.10") double minMargin) {
}
