package com.thomasvitale.mousike.ollama;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param model the audio-capable Ollama model that transcribes recordings
 */
@ConfigurationProperties(prefix = "mousike.ollama.transcription")
record OllamaTranscriptionProperties(@DefaultValue("lfm3-omni:2b") String model) {
}
