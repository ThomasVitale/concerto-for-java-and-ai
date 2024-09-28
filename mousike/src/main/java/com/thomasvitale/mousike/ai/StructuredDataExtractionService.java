package com.thomasvitale.mousike.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptionsBuilder;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
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
                .defaultOptions(ChatOptionsBuilder.builder()
                        .withModel("gpt-4o")
                        .withTemperature(0.0)
                        .build())
                .build();
    }

    public <T> T extract(String unstructuredData, Class<T> structuredType) {
        return extract(unstructuredData, structuredType, new String[]{});
    }

    public <T> T extract(String unstructuredData, Class<T> structuredType, String... functionNames) {
        return chatClient.prompt()
                .system(DEFAULT_STRUCTURED_DATA_EXTRACTION_PROMPT)
                .user(unstructuredData)
                .options(OpenAiChatOptions.builder()
                        .withResponseFormat(new OpenAiApi.ChatCompletionRequest.ResponseFormat(OpenAiApi.ChatCompletionRequest.ResponseFormat.Type.JSON_OBJECT))
                        .build())
                .functions(functionNames)
                .call()
                .entity(structuredType);
    }

}
