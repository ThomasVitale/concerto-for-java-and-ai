package com.thomasvitale.mousike.scenes.internal;

import com.thomasvitale.mousike.scenes.Transcript;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public class RecordingTranscriber {

    private final TranscriptionModel transcriptionModel;
    private final AudioConverter converter;

    RecordingTranscriber(TranscriptionModel transcriptionModel, AudioConverter converter) {
        this.transcriptionModel = transcriptionModel;
        this.converter = converter;
    }

    public Transcript transcribe(byte[] audio, String filename) {
        byte[] wav = converter.toWav(audio, filename);
        String wavName = filename.replaceAll("\\.[^.]*$", "") + ".wav";
        Resource resource = new ByteArrayResource(wav) {
            @Override
            public String getFilename() {
                return wavName;
            }
        };
        long start = System.nanoTime();
        AudioTranscriptionResponse response = transcriptionModel.call(new AudioTranscriptionPrompt(resource));
        Duration duration = Duration.ofNanos(System.nanoTime() - start);
        String text = response.getResult().getOutput();
        String model = response.getMetadata().getOrDefault("model", "transcription model");
        return new Transcript(text.strip(), model, duration);
    }

}
