package com.thomasvitale.mousike.agent;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

/**
 * Wraps a tool so the UI can narrate an agent's work: one line per call, in the composer's words, before the tool
 * runs. It also remembers every answer given in this run: a small model sometimes asks the same question again, and
 * gets the same answer back at once, with a note that it already has it.
 */
public final class ProgressToolCallback implements ToolCallback {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final ToolCallback delegate;
    private final Consumer<String> progress;
    private final Map<String, String> answers;

    public ProgressToolCallback(ToolCallback delegate, Consumer<String> progress, Map<String, String> answers) {
        this.delegate = delegate;
        this.progress = progress;
        this.answers = answers;
    }

    /** One memory shared by all the tools of one run. */
    public static Map<String, String> newMemory() {
        return new ConcurrentHashMap<>();
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        return call(toolInput, null);
    }

    @Override
    public String call(String toolInput, @Nullable ToolContext toolContext) {
        String name = delegate.getToolDefinition().name();
        String key = name + " " + toolInput.strip();
        String earlier = answers.get(key);
        if (earlier != null && delegate.getToolMetadata().returnDirect()) {
            return earlier;     // the caller reads a direct result as it is: nothing may be added to it
        }
        if (earlier != null) {
            progress.accept("Already asked: " + describe(name, toolInput).toLowerCase(Locale.ROOT));
            return "You already asked exactly this and the answer has not changed. Do not ask again; use it or move on. " + earlier;
        }
        progress.accept(describe(name, toolInput));
        String answer = delegate.call(toolInput, toolContext);
        answers.put(key, answer);
        return answer;
    }

    static String describe(String tool, String input) {
        String query = argument(input, "query");
        if (tool.endsWith("askNotebook")) {
            String question = argument(input, "question");
            return question == null ? "Reading your notes" : "Reading your notes: “" + question + "”";
        }
        if (tool.endsWith("playChordProgression")) {
            return "Voicing the chord progression";
        }
        if (tool.endsWith("searchNotebook")) {
            return query == null ? "Searching your notebook" : "Searching your notebook for “" + query + "”";
        }
        if (tool.endsWith("search_instruments")) {
            return query == null ? "Searching the instrument library" : "Searching the instrument library for “" + query + "”";
        }
        if (tool.endsWith("get_instrument")) {
            return "Looking up a preset in your library";
        }
        if (tool.endsWith("list_libraries")) {
            return "Listing your instrument libraries";
        }
        if (tool.endsWith("list_sound_types") || tool.endsWith("list_characters")) {
            return "Reading how your library is organised";
        }
        return "Calling " + tool;
    }

    private static @Nullable String argument(String input, String name) {
        try {
            JsonNode node = JSON.readTree(input).get(name);
            return node == null || node.isNull() ? null : node.asString();
        } catch (RuntimeException ex) {
            return null;
        }
    }

}
