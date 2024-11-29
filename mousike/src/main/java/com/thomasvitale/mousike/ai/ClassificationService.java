package com.thomasvitale.mousike.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
public class ClassificationService {

    public static final String DEFAULT_CLASSIFICATION_PROMPT = """
			Classify the type of the provided text.

			For example:

			Input: The celesta is a good fit for fantasy or mystery compositions, creating a sense of mystical and supernatural.
			Output: "INSTRUMENT"

			Input: The chord progression vi-IV-I-V is commonly used for epic scenes, such as action, battles, and scenes with pathos.
			Output: "HARMONY"

			Input: They're taking the hobbits to Isengard! To Isengard! To Isengard!
			Output: "UNKNOWN"
			""";

    private final ChatClient chatClient;

    public ClassificationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
				.defaultOptions(OpenAiChatOptions.builder()
                        .withModel("gpt-3.5-turbo")
                        .withTemperature(0.0)
                        .build())
				.build();
    }

    public <T> T classify(String textToClassify, Class<T> classificationType) {
		return chatClient.prompt()
                .system(DEFAULT_CLASSIFICATION_PROMPT)
                .user(user -> user
                        .text("""
                            ---------------------
                            {textToClassify}
                            ---------------------
                            """)
                        .param("textToClassify", textToClassify))
				.call()
				.entity(classificationType);
    }

}
