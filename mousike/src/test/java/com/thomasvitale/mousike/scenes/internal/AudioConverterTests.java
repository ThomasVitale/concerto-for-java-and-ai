package com.thomasvitale.mousike.scenes.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AudioConverterTests {

    private final AudioConverter converter = new AudioConverter();

    @Test
    void aWavPassesThroughUntouched() {
        byte[] wav = {1, 2, 3};

        assertThat(converter.toWav(wav, "take.WAV")).isSameAs(wav);
    }

    @Test
    @EnabledOnOs(OS.MAC)
    void otherAudioBecomesWav() throws IOException {
        byte[] converted = converter.toWav(silence(AudioFileFormat.Type.AIFF), "take.aiff");

        assertThat(new String(converted, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("RIFF");
    }

    @Test
    @EnabledOnOs(OS.MAC)
    void whatTheToolCannotReadFailsWithItsMessage() {
        assertThatThrownBy(() -> converter.toWav("not audio".getBytes(StandardCharsets.UTF_8), "take.mp3"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("take.mp3");
    }

    private static byte[] silence(AudioFileFormat.Type type) throws IOException {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(new byte[3200]), format, 1600), type, out);
        return out.toByteArray();
    }

}
