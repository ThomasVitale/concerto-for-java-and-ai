package com.thomasvitale.mousike.compositionnote.ai;

import java.util.List;

import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

public final class DenyChatClientResponse {

    public static ChatClientResponse create(String message) {
        return ChatClientResponse.builder()
                .chatResponse(ChatResponse.builder()
                        .generations(List.of(new Generation(AssistantMessage.builder().content(message).build())))
                        .build())
                .build();
    }

}
