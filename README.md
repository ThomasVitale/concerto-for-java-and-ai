# Concerto for Java and AI

[Slides](https://speakerdeck.com/thomasvitale/concerto-for-java-and-ai-building-production-ready-llm-applications)

## Stack

* Java 23 (with GraalVM)
* Spring Boot 3.3 (with Spring AI)
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

If you want to run the application as it was before AI-infusion, run it as follows.

```shell
./gradlew bootTestRun --args='--spring.profiles.active=plain'
```

The application relies on the native Testcontainers support in Spring Boot to spin up a PostgreSQL database with the pgvector extension,
and a Grafana LGTM service for observability.

Grafana is listening to port 3000. Check your container runtime to find the port to which is exposed to your localhost and access Grafana from http://localhost:<port>.
The credentials are `admin`/`admin`.

### Classification

```shell
http :8080/notes/composition/classify content="When scoring a dramatic scene, the piano can deliver strong emotions. Adjust the reverb to make the sound richer."
```

```shell
http :8080/notes/composition/classify content="A chord progression for mysterious or romantic scenes: i III VII v."
```

### Semantic Search

```shell
http :8080/notes/composition/semantic-search query="An instrument to deliver the melody in a dramatic scene"
```

### Question Answering

```shell
http :8080/notes/composition/ask query="What instrument can I use to deliver the melody in a dramatic scene?"
```

### Structured Data Extraction

```shell
http :8080/notes/director/structure text="The movie name is The Last Spring. Let's talk about the first scene, that takes place in a galaxy far far away. Spring is coming. Maybe for the last time. Let's dive right into my notes for each scene time. At seconds 0 0 there should be an uneasy high pitch music that starts rising, growing in intensity and generating suspense. At second 15 I am imagining some percussive instruments that fade in, accompanying the main character towards their inevitable destiny."
```

```shell
http :8080/notes/director/function text="The movie name is The Last Spring. Let's talk about the first scene, that takes place in a galaxy far far away. Spring is coming. Maybe for the last time. Let's dive right into my notes for each scene time. At seconds 0 0 there should be an uneasy high pitch music that starts rising, growing in intensity and generating suspense. At second 15 I am imagining some percussive instruments that fade in, accompanying the main character towards their inevitable destiny."
```

## Odai

Run the Spring Boot application (exposed through port 9090).

```shell
cd odai
./gradlew bootTestRun
```

The application relies on the native Testcontainers support in Spring Boot to spin up an Ollama service with a _mistral_ model at startup time.
