package com.thomasvitale.mousike.assistant;

import com.thomasvitale.mousike.agent.ProgressToolCallback;
import com.thomasvitale.mousike.assistant.internal.NotebookRagAgent.NotebookAnswer;
import com.thomasvitale.mousike.assistant.internal.NotebookRagTools;
import com.thomasvitale.mousike.chords.ChordProgression;
import com.thomasvitale.mousike.chords.ChordProgressionService;
import com.thomasvitale.mousike.chords.ChordProgressionTools;
import com.thomasvitale.mousike.chords.ChordProgressionTools.ChordProgressionResult;
import com.thomasvitale.mousike.guardrail.DecisionGuardrailAdvisor;
import com.thomasvitale.mousike.guardrail.GuardrailVerdict;
import com.thomasvitale.mousike.library.InstrumentLibrary;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.ai.session.compaction.TurnCountTrigger;
import org.springframework.ai.session.compaction.WindowCompactionStrategy;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
public class StudioAssistant {

    private static final String SYSTEM_PROMPT = """
            You are the assistant in a composer's studio, for scoring film, games and theatre. The composer writes \
            the music: you never write melodies, lyrics or full arrangements.
            Use your tools rather than your memory, and never invent chords or presets.
            When you name presets, show every one you found as a Markdown list, each with its name and library and \
            without its id, then add at most one sentence.
            When one missing detail stops you, ask one short question instead of guessing. Answer in short Markdown.""";

    private static final String LIBRARY_OFFLINE = """
            The instrument library is offline right now. If the composer asks about their instruments, say so.""";

    private final ChatClient chatClient;
    private final ToolCallback[] studioTools;
    private final ChordProgressionService chordProgressionService;
    private final InstrumentLibrary instrumentLibrary;
    private final JsonMapper json;

    public StudioAssistant(ChatClient.Builder builder, DecisionGuardrailAdvisor guardrail, SessionService sessionService, NotebookRagTools notebookRagTools, ChordProgressionTools chordProgressionTools,
                    ChordProgressionService chordProgressionService, InstrumentLibrary instrumentLibrary, JsonMapper json) {
        this.studioTools = ToolCallbacks.from(notebookRagTools, chordProgressionTools);
        this.chordProgressionService = chordProgressionService;
        this.instrumentLibrary = instrumentLibrary;
        this.json = json;
        List<Advisor> advisors = new ArrayList<>(SessionAdvisors.builder(sessionService)
                .compaction(new TurnCountTrigger(20), WindowCompactionStrategy.builder().lastTurns(10).build())
                .build());
        advisors.add(guardrail);
        this.chatClient = builder
                .defaultAdvisors(advisors)
                .build();
    }

    /** Answers one message of the conversation {@code sessionId}, reporting each tool call to {@code progress}. */
    public Answer reply(String sessionId, String message, Consumer<String> progress) {
        long start = System.nanoTime();
        ToolCallback[] libraryTools = instrumentLibrary.toolCallbacks();
        Map<String, String> answers = ProgressToolCallback.newMemory();

        ChatClientResponse response = chatClient.prompt()
                .system(libraryTools.length == 0 ? SYSTEM_PROMPT + "\n\n" + LIBRARY_OFFLINE : SYSTEM_PROMPT)
                .user(message)
                .advisors(SessionAdvisors.session(sessionId))
                .tools(Stream.concat(Arrays.stream(studioTools), Arrays.stream(libraryTools))
                        .map(tool -> new ProgressToolCallback(tool, progress, answers)).toArray())
                .call()
                .chatClientResponse();
        Duration duration = Duration.ofNanos(System.nanoTime() - start);

        ChatResponse chatResponse = response.chatResponse();
        List<String> texts = new ArrayList<>();
        List<Citation> citations = new ArrayList<>();
        List<ChordProgression> progressions = new ArrayList<>();
        Set<String> seen = new HashSet<>();     // the same direct tool call twice in one round is one answer
        // A tool that returns directly leaves its result in the response, named in the metadata: that result is what we show
        for (Generation generation : chatResponse == null ? List.<Generation>of() : chatResponse.getResults()) {
            String text = Objects.requireNonNullElse(generation.getOutput().getText(), "");
            if (!seen.add(text)) {
                continue;
            }
            Object tool = generation.getMetadata().get(ToolExecutionResult.METADATA_TOOL_NAME);
            if (NotebookRagTools.TOOL_NAME.equals(tool)) {
                parse(text, NotebookAnswer.class).ifPresentOrElse(answer -> {
                    texts.add(answer.markdown());
                    citations.addAll(answer.citations());
                }, () -> texts.add(text));
            }
            else if (ChordProgressionTools.TOOL_NAME.equals(tool)) {
                parse(text, ChordProgressionResult.class).ifPresentOrElse(result -> {
                    texts.add(result.whyItFits());
                    chordProgressionService.findById(result.progressionId()).ifPresent(progressions::add);
                }, () -> texts.add(text));
            }
            else {
                texts.add(text);
            }
        }
        String model = chatResponse == null || chatResponse.getMetadata().getModel().isBlank() ? "chat model" : chatResponse.getMetadata().getModel();
        GuardrailVerdict verdict = response.context().get(DecisionGuardrailAdvisor.VERDICT) instanceof GuardrailVerdict v ? v : null;
        return new Answer(String.join("\n\n", texts), citations, progressions, verdict,
                verdict != null && !verdict.allowed() ? verdict.model() : model, duration);
    }

    /** Empty when the tool failed: a direct tool's error message comes back in place of its result. */
    private <T> Optional<T> parse(String text, Class<T> type) {
        try {
            return Optional.of(json.readValue(text, type));
        }
        catch (JacksonException ex) {
            return Optional.empty();
        }
    }

}
