package com.thomasvitale.mousike.assistant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import com.thomasvitale.mousike.assistant.internal.NotebookRagAgent;
import com.thomasvitale.mousike.assistant.internal.NotebookRagAgent.NotebookAnswer;
import com.thomasvitale.mousike.assistant.internal.NotebookRagTools;
import com.thomasvitale.mousike.chords.ChordProgression;
import com.thomasvitale.mousike.chords.ChordProgressionService;
import com.thomasvitale.mousike.chords.ChordProgressionTools;
import com.thomasvitale.mousike.guardrail.DecisionGuardrailAdvisor;
import com.thomasvitale.mousike.library.InstrumentLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.session.DefaultSessionService;
import org.springframework.ai.session.InMemorySessionRepository;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.core.Ordered;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * How a reply reads the model's response, with the tools for real and a scripted model: this is the demo's main path.
 */
class StudioAssistantTests {

    private static final String CHORDS = "{\"title\":\"Chase\",\"key\":\"D minor\",\"numerals\":[\"%s\",\"VI\"],\"whyItFits\":\"Keeps moving.\"}";

    private final NotebookRagAgent notebook = mock(NotebookRagAgent.class);
    private final ChordProgressionService progressions = mock(ChordProgressionService.class);
    private final InstrumentLibrary library = mock(InstrumentLibrary.class);
    private final DecisionGuardrailAdvisor guardrail = mock(DecisionGuardrailAdvisor.class);

    @BeforeEach
    void setUp() {
        when(library.toolCallbacks()).thenReturn(new ToolCallback[0]);
        when(guardrail.getName()).thenReturn("guardrail");
        when(guardrail.getOrder()).thenReturn(Ordered.HIGHEST_PRECEDENCE);
        when(guardrail.adviseCall(any(), any())).thenAnswer(call -> call.<CallAdvisorChain>getArgument(1).nextCall(call.<ChatClientRequest>getArgument(0)));
    }

    @Test
    void aPlainAnswerIsTheModelsText() {
        Answer answer = assistantAnswering(new AssistantMessage("Hello.")).reply(UUID.randomUUID().toString(), "Hi", step -> { });

        assertThat(answer.markdown()).isEqualTo("Hello.");
        assertThat(answer.chordProgressions()).isEmpty();
    }

    @Test
    void aChordProgressionComesWithTheModelsWords() {
        ChordProgression progression = new ChordProgression(UUID.randomUUID(), "Chase", "D minor", 130, 4, "Dm Bb", "D4 F4 A4 | D4 F4 Bb4", new byte[0], null);
        when(progressions.render(anyString(), anyString(), anyList(), anyInt(), anyInt())).thenReturn(progression);
        when(progressions.findById(progression.requiredId())).thenReturn(Optional.of(progression));

        Answer answer = assistantAnswering(toolCalls("playChordProgression", CHORDS.formatted("i")))
                .reply(UUID.randomUUID().toString(), "Play it", step -> { });

        assertThat(answer.markdown()).isEqualTo("Keeps moving.");
        assertThat(answer.chordProgressions()).containsExactly(progression);
    }

    @Test
    void aFailingToolShowsItsMessageInsteadOfBreakingTheReply() {
        Answer answer = assistantAnswering(toolCalls("playChordProgression", CHORDS.formatted("nonsense!")))
                .reply(UUID.randomUUID().toString(), "Play it", step -> { });

        assertThat(answer.markdown()).contains("nonsense!");
        assertThat(answer.chordProgressions()).isEmpty();
    }

    @Test
    void theSameDirectCallTwiceIsOneAnswer() {
        when(notebook.answer("What about chases?")).thenReturn(new NotebookAnswer("Your note says 7/8.", List.of(new Citation(null, "RHYTHM", "7/8"))));
        String question = "{\"question\":\"What about chases?\"}";

        Answer answer = assistantAnswering(toolCalls("askNotebook", question, question))
                .reply(UUID.randomUUID().toString(), "Chases?", step -> { });

        assertThat(answer.markdown()).isEqualTo("Your note says 7/8.");
        assertThat(answer.citations()).hasSize(1);
    }

    private StudioAssistant assistantAnswering(AssistantMessage message) {
        ChatModel model = new ChatModel() {

            @Override
            public ChatResponse call(Prompt prompt) {
                return ChatResponse.builder().generations(List.of(new Generation(message))).build();
            }

            @Override
            public ChatOptions getOptions() {
                return ToolCallingChatOptions.builder().build();
            }

        };
        ChatClient.Builder builder = ChatClient.builder(model);
        return new StudioAssistant(builder, guardrail, DefaultSessionService.builder().sessionRepository(InMemorySessionRepository.builder().build()).build(),
                new NotebookRagTools(notebook), new ChordProgressionTools(progressions), progressions, library, JsonMapper.builder().build());
    }

    private static AssistantMessage toolCalls(String tool, String... arguments) {
        List<AssistantMessage.ToolCall> calls = IntStream.range(0, arguments.length)
                .mapToObj(i -> new AssistantMessage.ToolCall("call-" + i, "function", tool, arguments[i]))
                .toList();
        return AssistantMessage.builder().content("").toolCalls(calls).build();
    }

}
