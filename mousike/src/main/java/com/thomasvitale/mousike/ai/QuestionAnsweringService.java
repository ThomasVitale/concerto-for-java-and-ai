package com.thomasvitale.mousike.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class QuestionAnsweringService {

    private final ChatClient chatClient;

    public QuestionAnsweringService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().topK(3).build()))
                .defaultOptions(ChatOptions.builder()
                    .model("gpt-4o")
                    .temperature(0.0)
                    .build())
                .build();
    }

    public String answer(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

}
