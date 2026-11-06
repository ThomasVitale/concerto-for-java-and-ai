package com.thomasvitale.mousike.ollama;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.audio.transcription.AudioTranscriptionOptions;

public record OllamaTranscriptionOptions(
        /* The audio-capable Ollama model, e.g. lfm3-omni:2b. */
        String model,
        /* The spoken language as an ISO-639-1 code, e.g. "en"; detected when absent. */
        @Nullable String language,
        /* Text to guide the transcription, e.g. names or spelling the model should use. */
        @Nullable String prompt
) implements AudioTranscriptionOptions {

    public static OllamaTranscriptionOptions of(String model) {
        return new OllamaTranscriptionOptions(model, null, null);
    }

    @Override
    public String getModel() {
        return model;
    }

    /** These options as defaults, overridden by whatever a request sets. */
    OllamaTranscriptionOptions overriddenBy(@Nullable AudioTranscriptionOptions request) {
        if (request instanceof OllamaTranscriptionOptions(String newModel, String newLanguage, String newPrompt)) {
            return new OllamaTranscriptionOptions(newModel,
                    newLanguage != null ? newLanguage : language,
                    newPrompt != null ? newPrompt : prompt);
        }
        if (request != null) {
            return new OllamaTranscriptionOptions(request.getModel(), language, prompt);
        }
        return this;
    }

}
