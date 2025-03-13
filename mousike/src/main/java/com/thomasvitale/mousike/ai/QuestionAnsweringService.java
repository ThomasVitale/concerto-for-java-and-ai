package com.thomasvitale.mousike.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class QuestionAnsweringService {

    private final ChatClient chatClient;

    public QuestionAnsweringService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(RetrievalAugmentationAdvisor.builder()
                        .documentRetriever(VectorStoreDocumentRetriever.builder()
                                .similarityThreshold(0.50)
                                .topK(3)
                                .vectorStore(vectorStore)
                                .build())
                        .build())
                .defaultOptions(ChatOptions.builder()
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
