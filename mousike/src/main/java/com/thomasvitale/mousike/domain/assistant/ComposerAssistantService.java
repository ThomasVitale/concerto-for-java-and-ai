package com.thomasvitale.mousike.domain.assistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class ComposerAssistantService {

    private static final String SYSTEM_PROMPT = """
        You are a friendly assistant to a music composer who's working on the soundtrack for a movie.
        Always recommend three chord progressions.
        Then, provide a composition strategy step-by-step with at most 3 steps.
        Suggest musical instruments that match the feeling and emotions of the scene.
        For each instrument, verify that it's available and only use it if it is available.
    """;

    private static final String USER_PROMPT = """
        Suggest a composition strategy to score a video fragment of this movie scene.

        -------
        {scene}
        -------
    """;

    private final ChatClient chatClient;

    public ComposerAssistantService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultFunctions("isInstrumentAvailable")
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore,
                        SearchRequest.defaults()
                                .withFilterExpression("type == 'INSTRUMENT'")
                                .withTopK(5)))
                .defaultOptions(OpenAiChatOptions.builder()
                        .withResponseFormat(new OpenAiApi.ChatCompletionRequest.ResponseFormat(OpenAiApi.ChatCompletionRequest.ResponseFormat.Type.JSON_OBJECT))
                        .build())
                .build();
    }

    public CompositionPlan plan(SceneToScore sceneToScore) {
        return chatClient
                .prompt()
                .user(userSpec -> userSpec
                        .text(USER_PROMPT)
                        .param("scene", sceneToScore.toString()))
                .call()
                .entity(CompositionPlan.class);
    }

}
