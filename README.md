# Concerto for Java and AI

[Slides](https://speakerdeck.com/thomasvitale/concerto-for-java-and-ai-building-production-ready-llm-applications-bc8c4ab8-6384-4937-bfbc-b18c024bc924)

## Stack

* Java 25
* Spring Boot 4.2
* Spring AI 2.1
* Vaadin
* Arconia

## Mousike

### Ollama

The application consumes models from an [Ollama](https://ollama.ai) inference server. If you don't have an Ollama server running locally on your laptop, an Ollama service will be automatically spun up for you by the [Arconia Dev Services](https://docs.arconia.io/arconia/latest/dev-services/) when you run the application. Either way, Spring AI will take care of pulling the needed Ollama models when the application starts, if they are not available yet on your machine.

### Running the application

Run the application as follows:

```shell
./gradlew bootRun
```

Alternatively, you can use the [Arconia CLI](https://docs.arconia.io/arconia-cli/latest/index.html):

```shell
arconia dev
```

Under the hood, the Arconia framework will automatically spin up the needed backing services using [Arconia Dev Services](https://arconia.io/docs/arconia/latest/dev-services/) and Testcontainers:

* Docling document processor
* Grafana LGTM observability platform
* PostgreSQL database.

The application will be accessible at http://localhost:8080.

### Accessing Grafana

The application logs will show you the URL where you can access the Grafana observability platform.

```logs
...i.a.d.s.startup  : Dev Service 'lgtm' links: Grafana: http://localhost:<port>
```

By default, logs, metrics, and traces are exported via OTLP using the HTTP/Protobuf format.

In Grafana, you can query the telemetry from the "Drilldown" and "Explore" sections.
