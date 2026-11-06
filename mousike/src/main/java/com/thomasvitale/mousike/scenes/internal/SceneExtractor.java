package com.thomasvitale.mousike.scenes.internal;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.thomasvitale.mousike.scenes.Medium;
import com.thomasvitale.mousike.scenes.SceneDraft;
import com.thomasvitale.mousike.scenes.SceneMarker;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class SceneExtractor {

    static final String SYSTEM_PROMPT = """
            You turn the transcript of a director describing a scene into structured data. \
            A director is watching a scene with a composer and says what the music \
            should do at specific moments.
            Rules:
            - production: the name of the film, series, game or show the scene belongs to, if the director names it; \
              otherwise empty. It is not the scene's title.
            - title: the title the director gives the scene; otherwise a short descriptive title (max 6 words).
            - description: one or two sentences describing the scene itself, in neutral words.
            - genre: one or two words (e.g. "drama", "horror", "heist") if it can be inferred, otherwise empty.
            - markers: every moment the director mentions, in order. time is normalised to mm:ss \
              ("zero twelve" is 00:12, "one twenty" is 01:20, "two oh five" is 02:05). comment is the director's \
              request in their own words, trimmed.
            Never invent markers or musical ideas that are not in the transcript.""";

    private static final Logger logger = LoggerFactory.getLogger(SceneExtractor.class);

    private final ChatClient chatClient;

    SceneExtractor(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultOptions(ChatOptions.builder().temperature(0.0))
                .build();
    }

    public SceneDraft extract(String transcript, @Nullable Medium medium) {
        long start = System.nanoTime();
        ResponseEntity<ChatResponse, ExtractedScene> response = chatClient.prompt()
                .user(transcript)
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .call()
                .responseEntity(ExtractedScene.class);
        Duration duration = Duration.ofNanos(System.nanoTime() - start);
        ExtractedScene extracted = response.entity();
        ChatResponse chatResponse = response.response();
        String model = chatResponse == null || chatResponse.getMetadata().getModel().isBlank() ? "chat model" : chatResponse.getMetadata().getModel();
        if (logger.isDebugEnabled() && chatResponse != null && chatResponse.getResult() != null) {
            logger.debug("Scene extraction raw output: {}", chatResponse.getResult().getOutput().getText());
        }
        if (extracted == null) {
            return new SceneDraft("", "", "", medium == null ? Medium.FILM : medium, null, transcript, List.of(), model, duration);
        }
        List<SceneMarker> markers = extracted.markers() == null ? List.of() : extracted.markers().stream()
                .filter(m -> m.time() != null && m.comment() != null && !m.comment().isBlank())
                .map(m -> new SceneMarker(normalise(m.time()), capitalise(m.comment().strip())))
                .toList();
        return new SceneDraft(nullToEmpty(extracted.production()), nullToEmpty(extracted.title()), nullToEmpty(extracted.description()),
                medium == null ? Medium.FILM : medium, extracted.genre(), transcript, markers, model, duration);
    }

    static String normalise(String time) {
        String trimmed = time.strip();
        String[] parts = trimmed.split(":");
        try {
            if (parts.length == 1) {
                int seconds = Integer.parseInt(parts[0].replaceAll("\\D", ""));
                return "%02d:%02d".formatted(seconds / 60, seconds % 60);
            }
            if (parts.length == 2) {
                return "%02d:%02d".formatted(Integer.parseInt(parts[0].strip()), Integer.parseInt(parts[1].strip()));
            }
            return "%02d:%02d:%02d".formatted(Integer.parseInt(parts[0].strip()), Integer.parseInt(parts[1].strip()),
                    Integer.parseInt(parts[2].strip()));
        } catch (NumberFormatException ex) {
            return trimmed;
        }
    }

    /** The director's words as spoken, starting with a capital letter like a sentence; the rest is left untouched. */
    static String capitalise(String text) {
        if (text.isEmpty()) {
            return text;
        }
        int first = text.codePointAt(0);
        return new StringBuilder(text.length())
                .appendCodePoint(Character.toUpperCase(first))
                .append(text, Character.charCount(first), text.length())
                .toString();
    }

    private static String nullToEmpty(@Nullable String value) {
        return value == null ? "" : value.strip();
    }

    record ExtractedScene(
            @JsonPropertyDescription("Name of the film, series, game or show, empty if the director does not name it") String production,
            @JsonPropertyDescription("Short title of the scene") String title,
            @JsonPropertyDescription("One or two sentences describing what happens in the scene") String description,
            @JsonPropertyDescription("Genre in one or two words, empty if unknown") String genre,
            @JsonPropertyDescription("Every moment the director comments on, in order") List<ExtractedMarker> markers) {
    }

    record ExtractedMarker(
            @JsonPropertyDescription("Time in the scene as mm:ss") String time,
            @JsonPropertyDescription("What the director wants the music to do at this moment") String comment) {
    }

}
