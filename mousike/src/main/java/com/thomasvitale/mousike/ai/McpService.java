package com.thomasvitale.mousike.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
public class McpService {

    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider mcpToolCallbackProvider;

    public McpService(ChatClient.Builder chatClientBuilder, SyncMcpToolCallbackProvider mcpToolCallbackProvider) {
        this.chatClient = chatClientBuilder
            .build();
        this.mcpToolCallbackProvider = mcpToolCallbackProvider;
    }

    public String answer(String question) {
        return chatClient.prompt()
                .system("""
                    You are an agent that can use tools to perform the operations
                    requested by a musician and composer. When asked to perform an operation,
                    use the appropriate tool to perform the operation. Once the tool execution is
                    complete, return a brief summary to the user about whay the tool did.
                    """)
                .user(question)
                .toolCallbacks(mcpToolCallbackProvider)
                .options(ChatOptions.builder()
                        .model("mistral-large-latest")
                        .build())
                .call()
                .content();
    }

}
