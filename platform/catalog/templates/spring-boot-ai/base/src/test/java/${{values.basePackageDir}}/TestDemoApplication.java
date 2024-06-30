package ${{ values.basePackage }};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
{%- if values.vectorStore == "chroma" %}
import org.testcontainers.chromadb.ChromaDBContainer;
{%- endif %}
{%- if values.vectorStore == "weaviate" %}
import org.testcontainers.weaviate.WeaviateContainer;
{%- endif %}

{%- if values.llmProvider == "ollama" %}
import io.thomasvitale.langchain4j.testcontainers.service.containers.OllamaContainer;
{%-endif %}

@TestConfiguration(proxyBeanMethods = false)
public class TestDemoApplication {

	{%- if values.llmProvider == "ollama" %}
	@Bean
	@RestartScope
	@ServiceConnection
	OllamaContainer ollama() {
		return new OllamaContainer("ghcr.io/thomasvitale/ollama-llama2");
	}
	{%- endif %}

	{%- if values.vectorStore == "chroma" %}
	@Bean
	@RestartScope
	@ServiceConnection
	ChromaDBContainer chroma() {
		return new ChromaDBContainer("ghcr.io/chroma-core/chroma:0.4.24");
	}
	{%- endif %}

	{%- if values.vectorStore == "weaviate" %}
	@Bean
	@RestartScope
	@ServiceConnection
	WeaviateContainer weaviate() {
		return new WeaviateContainer("semitechnologies/weaviate:1.24.1");
	}
	{%- endif %}

	public static void main(String[] args) {
		SpringApplication.from(DemoApplication::main).with(TestDemoApplication.class).run(args);
	}

}
