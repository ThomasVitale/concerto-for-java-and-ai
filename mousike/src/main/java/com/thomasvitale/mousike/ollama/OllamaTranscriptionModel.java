package com.thomasvitale.mousike.ollama;

import io.micrometer.observation.ObservationRegistry;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponseMetadata;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.transcription.observation.DefaultTranscriptionModelObservationConvention;
import org.springframework.ai.audio.transcription.observation.TranscriptionModelObservationContext;
import org.springframework.ai.audio.transcription.observation.TranscriptionModelObservationConvention;
import org.springframework.ai.audio.transcription.observation.TranscriptionModelObservationDocumentation;
import org.springframework.ai.observation.conventions.AiProvider;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Objects;

public final class OllamaTranscriptionModel implements TranscriptionModel {

    public static final String MODEL = "model";

    private static final TranscriptionModelObservationConvention DEFAULT_OBSERVATION_CONVENTION =
            new DefaultTranscriptionModelObservationConvention();

    private final RestClient restClient;
    private final OllamaTranscriptionOptions defaultOptions;
    private final ObservationRegistry observationRegistry;
    private final TranscriptionModelObservationConvention observationConvention;

    private OllamaTranscriptionModel(RestClient restClient,
                                     OllamaTranscriptionOptions defaultOptions,
                                     ObservationRegistry observationRegistry,
                                     TranscriptionModelObservationConvention observationConvention
    ) {
        this.restClient = restClient;
        this.defaultOptions = defaultOptions;
        this.observationRegistry = observationRegistry;
        this.observationConvention = observationConvention;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public AudioTranscriptionResponse call(AudioTranscriptionPrompt transcriptionPrompt) {
        OllamaTranscriptionOptions options = defaultOptions.overriddenBy(transcriptionPrompt.getOptions());
        Resource audio = transcriptionPrompt.getInstructions();
        TranscriptionModelObservationContext observationContext = TranscriptionModelObservationContext.builder()
                .prompt(new AudioTranscriptionPrompt(audio, options))
                .provider(AiProvider.OLLAMA.value())
                .build();

        return Objects.requireNonNull(TranscriptionModelObservationDocumentation.TRANSCRIPTION_MODEL_OPERATION
                .observation(observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext, observationRegistry)
                .observe(() -> {
                    TranscriptionResult result = restClient.post()
                            .uri("/v1/audio/transcriptions")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .body(form(audio, options))
                            .retrieve()
                            .body(TranscriptionResult.class);
                    AudioTranscriptionResponseMetadata metadata = new AudioTranscriptionResponseMetadata();
                    metadata.put(MODEL, options.model());
                    AudioTranscriptionResponse response = new AudioTranscriptionResponse(
                            new AudioTranscription(result == null || result.text() == null ? "" : result.text().strip()), metadata);
                    observationContext.setResponse(response);
                    return response;
                }));
    }

    private static MultiValueMap<String, HttpEntity<?>> form(Resource audio, OllamaTranscriptionOptions options) {
        MultipartBodyBuilder form = new MultipartBodyBuilder();
        String filename = audio.getFilename();
        form.part("file", audio).filename(filename == null ? "audio.wav" : filename);
        form.part("model", options.model());
        if (options.language() != null) {
            form.part("language", options.language());
        }
        if (options.prompt() != null) {
            form.part("prompt", options.prompt());
        }
        return form.build();
    }

    public static final class Builder {

        private @Nullable RestClient restClient;
        private @Nullable OllamaTranscriptionOptions defaultOptions;
        private ObservationRegistry observationRegistry = ObservationRegistry.NOOP;
        private TranscriptionModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

        private Builder() {}

        public Builder restClient(RestClient restClient) {
            this.restClient = restClient;
            return this;
        }

        public Builder options(OllamaTranscriptionOptions defaultOptions) {
            this.defaultOptions = defaultOptions;
            return this;
        }

        public Builder observationRegistry(ObservationRegistry observationRegistry) {
            this.observationRegistry = observationRegistry;
            return this;
        }

        public Builder observationConvention(TranscriptionModelObservationConvention observationConvention) {
            this.observationConvention = observationConvention;
            return this;
        }

        public OllamaTranscriptionModel build() {
            Assert.notNull(restClient, "restClient cannot be null");
            Assert.notNull(defaultOptions, "defaultOptions cannot be null");
            return new OllamaTranscriptionModel(restClient, defaultOptions, observationRegistry, observationConvention);
        }

    }

    record TranscriptionResult(@Nullable String text) {}

}
