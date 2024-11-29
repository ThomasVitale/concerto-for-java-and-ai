package com.thomasvitale.mousike;

import java.time.Duration;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.grafana.LgtmStackContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @RestartScope
    @ServiceConnection
    PostgreSQLContainer<?> pgvectorContainer() {
        return new PostgreSQLContainer<>("pgvector/pgvector:pg17");
    }

    @Bean
    @RestartScope
    @ServiceConnection
    LgtmStackContainer lgtmContainer() {
        return new LgtmStackContainer("grafana/otel-lgtm:0.8.0")
                .withStartupTimeout(Duration.ofMinutes(2))
                .withReuse(true);
    }

}
