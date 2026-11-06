package com.thomasvitale.mousike.ollama;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.observation.DefaultTranscriptionModelObservationConvention;
import org.springframework.ai.audio.transcription.observation.TranscriptionModelObservationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OllamaTranscriptionModelTests {

    private final List<TranscriptionModelObservationContext> observed = new ArrayList<>();

    private MockRestServiceServer ollama;
    private RestClient restClient;
    private OllamaTranscriptionModel model;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:11434");
        ollama = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
        ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(new ObservationHandler<TranscriptionModelObservationContext>() {
            @Override
            public void onStop(TranscriptionModelObservationContext context) {
                observed.add(context);
            }

            @Override
            public boolean supportsContext(Observation.Context context) {
                return context instanceof TranscriptionModelObservationContext;
            }
        });
        model = OllamaTranscriptionModel.builder()
                .restClient(restClient)
                .options(OllamaTranscriptionOptions.of("lfm3-omni:2b"))
                .observationRegistry(registry)
                .build();
    }

    @Test
    void postsTheAudioToOllamasTranscriptionEndpoint() {
        ollama.expect(requestTo("http://localhost:11434/v1/audio/transcriptions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().multipartDataContains(Map.of("model", "lfm3-omni:2b")))
                .andExpect(request -> assertThat(request.getBody().toString()).isNotEmpty())
                .andRespond(withSuccess("{\"text\": \" At 1 minute 5, she smiles anyway. \"}", MediaType.APPLICATION_JSON));

        AudioTranscriptionResponse response = model.call(new AudioTranscriptionPrompt(wav("director.wav")));

        assertThat(response.getResult().getOutput()).isEqualTo("At 1 minute 5, she smiles anyway.");
        assertThat(response.getMetadata().<String>get(OllamaTranscriptionModel.MODEL)).isEqualTo("lfm3-omni:2b");
        ollama.verify();
    }

    @Test
    void requestOptionsOverrideTheDefaults() {
        ollama.expect(requestTo("http://localhost:11434/v1/audio/transcriptions"))
                .andExpect(content().multipartDataContains(Map.of("model", "other-model", "language", "en")))
                .andRespond(withSuccess("{\"text\": \"ok\"}", MediaType.APPLICATION_JSON));

        AudioTranscriptionResponse response = model.call(new AudioTranscriptionPrompt(wav("clip.wav"),
                new OllamaTranscriptionOptions("other-model", "en", null)));

        assertThat(response.getMetadata().<String>get(OllamaTranscriptionModel.MODEL)).isEqualTo("other-model");
        ollama.verify();
    }

    @Test
    void observesEachCallAsAnOllamaTranscription() {
        ollama.expect(requestTo("http://localhost:11434/v1/audio/transcriptions"))
                .andRespond(withSuccess("{\"text\": \"ok\"}", MediaType.APPLICATION_JSON));

        model.call(new AudioTranscriptionPrompt(wav("clip.wav")));

        assertThat(observed).singleElement().satisfies(context -> {
            assertThat(context.getOperationMetadata().provider()).isEqualTo("ollama");
            assertThat(context.getRequest().getOptions().getModel()).isEqualTo("lfm3-omni:2b");
            assertThat(context.getResponse()).isNotNull();
        });
    }

    @Test
    void usesACustomObservationConvention() {
        ollama.expect(requestTo("http://localhost:11434/v1/audio/transcriptions"))
                .andRespond(withSuccess("{\"text\": \"ok\"}", MediaType.APPLICATION_JSON));
        ObservationRegistry registry = ObservationRegistry.create();
        List<String> names = new ArrayList<>();
        registry.observationConfig().observationHandler(new ObservationHandler<>() {
            @Override
            public void onStop(Observation.Context context) {
                names.add(context.getName());
            }

            @Override
            public boolean supportsContext(Observation.Context context) {
                return true;
            }
        });
        OllamaTranscriptionModel customised = OllamaTranscriptionModel.builder()
                .restClient(restClient)
                .options(OllamaTranscriptionOptions.of("lfm3-omni:2b"))
                .observationRegistry(registry)
                .observationConvention(new DefaultTranscriptionModelObservationConvention() {
                    @Override
                    public String getName() {
                        return "mousike.transcription";
                    }
                })
                .build();

        customised.call(new AudioTranscriptionPrompt(wav("clip.wav")));

        assertThat(names).contains("mousike.transcription");
    }

    @Test
    void requiresAClientAndDefaultOptions() {
        assertThatIllegalArgumentException().isThrownBy(() -> OllamaTranscriptionModel.builder()
                .options(OllamaTranscriptionOptions.of("lfm3-omni:2b"))
                .build()).withMessageContaining("restClient");
        assertThatIllegalArgumentException().isThrownBy(() -> OllamaTranscriptionModel.builder()
                .restClient(RestClient.create())
                .build()).withMessageContaining("defaultOptions");
    }

    private static Resource wav(String filename) {
        return new ByteArrayResource(new byte[] { 'R', 'I', 'F', 'F' }) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

}
