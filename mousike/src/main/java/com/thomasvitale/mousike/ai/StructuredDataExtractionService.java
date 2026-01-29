package com.thomasvitale.mousike.ai;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

@Service
public class StructuredDataExtractionService {

    public static final String DEFAULT_STRUCTURED_DATA_EXTRACTION_PROMPT = """
			Extract structured data from the provided text.
			If you do not know the value of a field asked to extract,
			do not include any value for the field in the result.
			""";

    private final ChatClient chatClient;

    public StructuredDataExtractionService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .defaultOptions(ChatOptions.builder()
                        .temperature(0.0)
                        .build())
                .build();
    }

    @Nullable
    public <T> T extract(String unstructuredData, Class<T> structuredType) {
        return chatClient.prompt()
                .system(DEFAULT_STRUCTURED_DATA_EXTRACTION_PROMPT)
                .user(unstructuredData)
                .call()
                .entity(structuredType);
    }

}
