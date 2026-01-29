# Concerto for Java and AI

[Slides](https://speakerdeck.com/thomasvitale/concerto-for-java-and-ai-building-production-ready-llm-applications-bc8c4ab8-6384-4937-bfbc-b18c024bc924)

## Stack

* Java 25 (with GraalVM)
* Spring Boot 4.0 (with Spring AI)
* Vaadin
* Arconia

## Mousike

### Mistral AI

The application consumes chat and embedding models from the [Mistral AI](https://mistral.ai) platform.

#### Create an account

Visit [console.mistral.ai](https://console.mistral.ai) and sign up for a new account.
You can choose the "Experiment" plan, which gives you access to the Mistral APIs for free.

#### Configure API Key

In the Mistral AI console, navigate to _API Keys_ and generate a new API key.
Copy and securely store your API key on your machine as an environment variable.
The application will use it to access the Mistral AI API.

```shell
export SPRING_AI_MISTRAL_AI_API_KEY=<YOUR-API-KEY>
```

### OpenAI

The application consumes models from the [OpenAI](https://openai.com) platform.

#### Create an account

Visit [platform.openai.com](https://platform.openai.com) and sign up for a new account.

#### Configure API Key

In the OpenAI console, navigate to _Dashboard > API Keys_ and generate a new API key.
Copy and securely store your API key on your machine as an environment variable.
The application will use it to access the OpenAI API.

```shell
export SPRING_AI_ OPENAI_API_KEY=<YOUR-API-KEY>
```

### Running the application

Run the application.

```shell
./gradlew bootRun
```

Alternatively, you can use the [Arconia CLI](https://docs.arconia.io/arconia-cli/latest/index.html):

```shell
arconia dev
```

Under the hood, the Arconia framework will automatically spin up the needed backing services using [Arconia Dev Services](https://arconia.io/docs/arconia/latest/dev-services/) and Testcontainers:

* Docling document processor
* Phoenix AI observability platform
* PostgreSQL database.

The application will be accessible at http://localhost:8080.

### Accessing Phoenix

The application logs will show you the URL where you can access the Phoenix AI observability platform.

```logs
...Phoenix UI: http://localhost:<port>
```

By default, traces are exported via OTLP using the HTTP/Protobuf format.
