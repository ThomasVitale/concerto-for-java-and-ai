package com.thomasvitale.mousike.ollama;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.model.ollama.autoconfigure.OllamaConnectionDetails;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OllamaTranscriptionProperties.class)
class OllamaTranscriptionConfiguration {

    @Bean
    OllamaTranscriptionModel ollamaTranscriptionModel(
            OllamaConnectionDetails connectionDetails,
            RestClient.Builder restClientBuilder,
            OllamaTranscriptionProperties properties,
            ObjectProvider<ObservationRegistry> observationRegistry
    ) {
        RestClient restClient = restClientBuilder.clone()
                .baseUrl(connectionDetails.getBaseUrl())
                .build();
        return OllamaTranscriptionModel.builder()
                .restClient(restClient)
                .options(OllamaTranscriptionOptions.of(properties.model()))
                .observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
                .build();
    }

}
