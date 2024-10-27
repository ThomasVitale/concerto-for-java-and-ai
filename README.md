# Concerto for Java and AI

[Slides](https://speakerdeck.com/thomasvitale/concerto-for-java-and-ai-building-production-ready-llm-applications-bc8c4ab8-6384-4937-bfbc-b18c024bc924)

## Stack

* Java 23 (with GraalVM)
* Spring Boot 3.4 (with Spring AI)
* Vaadin

## Mousike

First, make sure you have an [OpenAI account](https://platform.openai.com/signup).
Then, define an environment variable with the OpenAI API Key associated to your OpenAI account as the value.

```shell
export SPRING_AI_OPENAI_API_KEY=<INSERT KEY HERE>
```

Finally, run the Spring Boot application.

```shell
cd mousike
./gradlew bootTestRun
```

You can access the application at http://localhost:8080.

The application relies on the native Testcontainers support in Spring Boot to spin up a PostgreSQL database with the pgvector extension,
and a Grafana LGTM service for observability.

Grafana is listening to port 3000. Check your container runtime to find the port to which is exposed to your localhost and access Grafana from http://localhost:<port>.
The credentials are `admin`/`admin`.
