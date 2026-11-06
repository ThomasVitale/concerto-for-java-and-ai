package com.thomasvitale.mousike.scenes.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
class AudioConverter {

    private static final Logger log = LoggerFactory.getLogger(AudioConverter.class);

    private static final Duration CONVERSION_TIMEOUT = Duration.ofMinutes(2);

    /**
     * Returns 16 kHz mono PCM WAV bytes for any audio the local tools can read; WAV input passes through untouched.
     */
    byte[] toWav(byte[] audio, String filename) {
        if (filename.toLowerCase(Locale.ROOT).endsWith(".wav")) {
            return audio;
        }
        String extension = filename.contains(".") ? filename.substring(filename.lastIndexOf('.')) : ".bin";
        try {
            Path input = Files.createTempFile("mousike-audio-", extension);
            Path output = Files.createTempFile("mousike-audio-", ".wav");
            Path toolOutput = Files.createTempFile("mousike-audio-", ".log");
            try {
                Files.write(input, audio);
                Files.deleteIfExists(output);
                List<String> command = command(input, output);
                log.debug("Converting {} to WAV with {}", filename, command.getFirst());
                // To a file: reading the tool's output here would block until it ends, whatever the timeout says
                Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(toolOutput.toFile()).start();
                if (!process.waitFor(CONVERSION_TIMEOUT)) {
                    process.destroyForcibly();
                    throw new IllegalStateException("Converting %s to WAV took too long".formatted(filename));
                }
                if (process.exitValue() != 0) {
                    throw new IllegalStateException("Could not convert %s to WAV: %s".formatted(filename, Files.readString(toolOutput).strip()));
                }
                return Files.readAllBytes(output);
            } finally {
                Files.deleteIfExists(input);
                Files.deleteIfExists(output);
                Files.deleteIfExists(toolOutput);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not convert %s to WAV".formatted(filename), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while converting %s to WAV".formatted(filename), ex);
        }
    }

    private static List<String> command(Path input, Path output) {
        Optional<Path> afconvert = executable("afconvert");
        if (afconvert.isPresent()) {
            return List.of(afconvert.get().toString(), "-f", "WAVE", "-d", "LEI16@16000", "-c", "1", input.toString(), output.toString());
        }
        Optional<Path> ffmpeg = executable("ffmpeg");
        if (ffmpeg.isPresent()) {
            return List.of(ffmpeg.get().toString(), "-loglevel", "error", "-y", "-i", input.toString(), "-ar", "16000", "-ac", "1",
                    "-c:a", "pcm_s16le", output.toString());
        }
        throw new IllegalStateException("Only WAV recordings can be transcribed on this machine: install ffmpeg to accept MP3 and M4A");
    }

    private static Optional<Path> executable(String tool) {
        String path = System.getenv().getOrDefault("PATH", "") + File.pathSeparator + "/usr/bin" + File.pathSeparator + "/opt/homebrew/bin";
        return Arrays.stream(path.split(File.pathSeparator))
                .filter(dir -> !dir.isBlank())
                .map(dir -> Path.of(dir, tool))
                .filter(Files::isExecutable)
                .findFirst();
    }

}
