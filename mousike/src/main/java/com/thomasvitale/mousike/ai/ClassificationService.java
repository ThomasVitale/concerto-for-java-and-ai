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

			Input: A chord progression for driving and triumphant scenes: i v VII IV.
			Output: "HARMONY"

			Input: They're taking the hobbits to Isengard! To Isengard! To Isengard!
			Output: "UNKNOWN"

			---------------------
			TEXT:
			{text}
			---------------------
			""";

    private final ChatClient chatClient;

    public ClassificationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
				.defaultOptions(OpenAiChatOptions.builder()
						.withTemperature(0.0f)
						.build())
				.build();
    }

    public <T> T classify(String textToClassify, Class<T> classificationType) {
		return chatClient.prompt()
				.user(userSpec -> userSpec
						.text(DEFAULT_CLASSIFICATION_PROMPT)
						.param("text", textToClassify)
				)
				.call()
				.entity(classificationType);
    }

}
