package com.thomasvitale.mousike.ai.observations;

import com.thomasvitale.mousike.ai.observations.aspects.ChatObservationAspect;

import com.thomasvitale.mousike.ai.observations.aspects.EmbeddingObservationAspect;
import com.thomasvitale.mousike.ai.observations.chat.ChatPromptObservationFilter;

import io.micrometer.observation.ObservationRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for Spring AI observations.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ ObservationsProperties.class })
public class ObservationsAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ObservationsAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = ObservationsProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    ChatObservationAspect chatObservationAspect(ObservationRegistry observationRegistry) {
        return new ChatObservationAspect(observationRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = ObservationsProperties.CONFIG_PREFIX, name = "include-prompt", havingValue = "true", matchIfMissing = false)
    ChatPromptObservationFilter chatPromptObservationFilter() {
        logger.warn("You have enabled the inclusion of the prompt content in the observations, with the risk of exposing sensitive or private information. Please, be careful!");
        return new ChatPromptObservationFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = ObservationsProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    EmbeddingObservationAspect embeddingObservationAspect(ObservationRegistry observationRegistry) {
        return new EmbeddingObservationAspect(observationRegistry);
    }

}
