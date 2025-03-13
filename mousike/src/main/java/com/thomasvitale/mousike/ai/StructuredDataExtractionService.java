package com.thomasvitale.mousike.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.stereotype.Service;

@Service
public class StructuredDataExtractionService {

    public static final String DEFAULT_STRUCTURED_DATA_EXTRACTION_PROMPT = """
			Extract structured data from the provided text.
			If you do not know the value of a field asked to extract,
			do not include any value for the field in the result.
			Finally, save the object in the database.
			""";

    private final ChatClient chatClient;

    public StructuredDataExtractionService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultOptions(ChatOptions.builder()
                        .temperature(0.0)
                        .build())
                .build();
    }

    public <T> T extract(String unstructuredData, Class<T> structuredType) {
        return chatClient.prompt()
                .system(DEFAULT_STRUCTURED_DATA_EXTRACTION_PROMPT)
                .user(unstructuredData)
                .options(MistralAiChatOptions.builder()
                        .responseFormat(new MistralAiApi.ChatCompletionRequest.ResponseFormat("json_object"))
                        .build())
                .call()
                .entity(structuredType);
    }

}
