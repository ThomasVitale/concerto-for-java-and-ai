package com.thomasvitale.mousike.compositionplan.internal;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;

/**
 * The plan as the model writes it, one record per step: plain non-null fields, because nullable fields become JSON-schema
 * type unions that small local models handle badly under grammar-constrained decoding.
 */
public final class PlanDraft {

    /** The cap: a small model can generate structured output without end, and that must fail, not hang the plan. */
    private static final int MAX_STRUCTURED_TOKENS = 1500;

    private PlanDraft() {
    }

    static ChatClient.Builder structured(ChatClient.Builder builder, double temperature) {
        return builder.defaultOptions(OllamaChatOptions.builder()
                .temperature(temperature)
                .numPredict(MAX_STRUCTURED_TOKENS));
    }

    static <T> List<T> nullSafe(@Nullable List<T> list) {
        return list == null ? List.of() : list;
    }

    static boolean blank(@Nullable String value) {
        return value == null || value.isBlank();
    }

    static String orElse(@Nullable String value, String fallback) {
        return blank(value) ? fallback : value.strip();
    }

    public record Harmony(
            @JsonPropertyDescription("The chord progressions you suggest") List<Progression> progressions) {
    }

    public record Progression(

            @JsonPropertyDescription("A name of three or four words, without numerals, chords or who wrote it, e.g. 'Noir half-step'") String name,
            @JsonPropertyDescription("The key, e.g. 'D minor'") String key,
            @JsonPropertyDescription("The chords in order, e.g. ['Dm', 'Bb', 'F', 'C']") List<String> chords,
            @JsonPropertyDescription("Why it fits this scene, in one sentence") String why) {
    }

    public record Palette(
            @JsonPropertyDescription("The instruments to use, one entry per role") List<Instrument> instruments) {
    }

    public record Instrument(

            @JsonPropertyDescription("The sound's job in the music, e.g. 'low pulse', 'theme', 'texture', not a time") String role,
            @JsonPropertyDescription("The kind of sound in plain words, e.g. 'solo cello', 'low drone', 'felt piano'") String instrument,
            @JsonPropertyDescription("The preset id the list gives for this sound, 0 if none") long presetId,
            @JsonPropertyDescription("What it does in this scene, in one sentence, without naming a preset or the scene's title") String why) {
    }

    public record Arc(
            @JsonPropertyDescription("The idea of the music in two sentences") String concept,
            @JsonPropertyDescription("The orchestration and arrangement suggested at each of the scene's time markers, in order") List<Moment> moments) {
    }

    public record Moment(

            @JsonPropertyDescription("The time marker exactly as the scene writes it, e.g. '00:07', never a range") String timeMarker,
            @JsonPropertyDescription("One or two full sentences of orchestration and arrangement: which sounds enter at this moment and how they build") String instruction,
            @JsonPropertyDescription("The sounds playing here, in plain words") List<String> instruments) {
    }

}
