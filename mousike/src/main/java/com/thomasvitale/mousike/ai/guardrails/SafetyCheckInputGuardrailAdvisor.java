package com.thomasvitale.mousike.ai.guardrails;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;

public class SafetyCheckInputGuardrailAdvisor implements CallAdvisor {

    private final ChatClient chatClient;

    public SafetyCheckInputGuardrailAdvisor(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        UserMessage userMessage = chatClientRequest.prompt().getUserMessage();

        Allowed allowed = chatClient.prompt()
                .system("""
                        You are a safety guard, making sure that the user question
                        doesn't involve pizza with pineapple. If the user asks
                        about pizza with pineapple, you should disallow it.
                        If the user question involves anything else,
                        you should allow it.
                        """)
                .user(userMessage.getText())
                .call()
                .entity(Allowed.class);

        if (Allowed.TRUE.equals(allowed)) {
            return callAdvisorChain.nextCall(chatClientRequest);
        } else {
            return DenyChatClientResponse.create("Computer says no!");
        }
    }

    public enum Allowed {
        TRUE, FALSE;
    }

    @Override
    public String getName() {
        return SafetyCheckInputGuardrailAdvisor.class.getName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ChatClient.Builder chatClientBuilder;

        private Builder() {}

        public Builder chatClientBuilder(ChatClient.Builder chatClientBuilder) {
            this.chatClientBuilder = chatClientBuilder;
            return this;
        }

        public SafetyCheckInputGuardrailAdvisor build() {
            return new SafetyCheckInputGuardrailAdvisor(chatClientBuilder);
        }
    }

}
