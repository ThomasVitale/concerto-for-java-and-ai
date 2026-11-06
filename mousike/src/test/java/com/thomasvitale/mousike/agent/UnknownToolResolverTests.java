package com.thomasvitale.mousike.agent;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;

import static org.assertj.core.api.Assertions.assertThat;

class UnknownToolResolverTests {

    @Test
    void anInventedToolNameGetsAnAnswerInsteadOfFailingTheRequest() {
        ToolCallingManager manager = ToolCallingManager.builder()
                .toolCallbackResolver(new UnknownToolResolver())
                .resolutionFallbackEnabled(true)
                .build();
        AssistantMessage call = AssistantMessage.builder()
                .content("")
                .toolCalls(List.of(new AssistantMessage.ToolCall("1", "function", "searchInstruments", "{}")))
                .build();

        ToolExecutionResult result = manager.executeToolCalls(new Prompt(new UserMessage("Find a cello"), ToolCallingChatOptions.builder().build()),
                ChatResponse.builder().generations(List.of(new Generation(call))).build());

        assertThat(result.returnDirect()).isFalse();
        assertThat(result.conversationHistory().getLast()).isInstanceOfSatisfying(ToolResponseMessage.class,
                response -> assertThat(response.getResponses().getFirst().responseData()).contains("no tool named 'searchInstruments'"));
    }

}
