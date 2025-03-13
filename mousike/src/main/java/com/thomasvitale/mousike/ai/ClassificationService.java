package com.thomasvitale.mousike.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

@Service
public class ClassificationService {

    private static final Logger logger = LoggerFactory.getLogger(ClassificationService.class);

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
				.defaultOptions(ChatOptions.builder()
                        .temperature(0.0)
                        .build())
				.build();
    }

    public <T> T classify(String textToClassify, Class<T> classificationType) {
        logger.info("Classifying text");
        return chatClient.prompt()
                .system(DEFAULT_CLASSIFICATION_PROMPT)
                .user(textToClassify)
				.call()
				.entity(classificationType);
    }

}
