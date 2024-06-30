package com.example.demo;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
{%- if values.vectorStore == "postgresql" %}
import org.testcontainers.containers.PostgreSQLContainer;
{%- endif %}
{% if values.llmProvider == "ollama" %}
import org.testcontainers.ollama.OllamaContainer;
{%- endif %}
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	{% if values.llmProvider == "ollama" %}
	@Bean
	@RestartScope
	@ServiceConnection
	OllamaContainer ollama() {
		return new OllamaContainer(DockerImageName.parse("ghcr.io/thomasvitale/ollama-mistral")
			.asCompatibleSubstituteFor("ollama/ollama"));
	}
	{%- endif %}

	{%- if values.vectorStore == "postgresql" %}
	@Bean
	@RestartScope
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));
	}
	{%- endif %}

}
