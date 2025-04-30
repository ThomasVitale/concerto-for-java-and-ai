package com.thomasvitale.mousike.domain.assistant;

import com.thomasvitale.mousike.ai.InstrumentTools;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class ComposerAssistantService {

    private static final String SYSTEM_PROMPT = """
        You are a composer tasked with creating a music composition plan for a movie scene.
        Based on the provided scene description, movie genre, and the retrieved list of available instruments,
        your goal is to suggest:

        1. Three possible chord progressions that match the mood, energy, and emotion of the scene.
        2. A three-step orchestration strategy explaining how you would structure the composition using the available instruments.

        Please, ensure that:

        The chord progressions reflect the tone of the scene and adheres to the movie genre, whether it's suspense, action, or emotion.
        The orchestration strategy details how the instruments will be layered or introduced at different stages to create atmosphere,
        build tension, or evoke emotions. If rythmic instruments are suggested (e.g. percussions, harp, piano), mention them first.

        Instructions:

        - Retrieve the list of instruments available for use.
        - Create chord progressions that match the scene's emotional arc.
        - In the orchestration strategy, explain how to apply the instruments in a layered, structured way.
          Only suggest instruments from the list of available instruments.

        -------
        Example

        Movie Scene Description: "In a high-octane car chase through a sprawling futuristic city,
        neon lights flash by as the hero and villain race through crowded streets, narrowly avoiding
        collisions and weaving between towering skyscrapers. The stakes are high as they battle
        for control over a stolen piece of advanced technology."

        Use the function to retrieve the list of available instruments.

        Expected Output:

        Chord Progressions:
        VII-VI-i-v
        ii-VI-iv-V
        i-VII-v-IV

        Orchestration Strategy Steps:
        - Begin with percussions to create a steady, driving rhythm that mirrors the intensity of the chase.
        - Add strings to highlight sharp turns, near-misses, and the escalating tension between the hero and villain.
        - Layer in cello to intensify the climax for deep, intense tones.
        """;

    private static final String USER_PROMPT = """
        Scene description:

        -------
        {scene}
        -------
    """;

    private final ChatClient chatClient;

    public ComposerAssistantService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, InstrumentTools instrumentTools) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(instrumentTools)
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore,
                        SearchRequest.builder()
                                //.filterExpression("type == 'INSTRUMENT'")
                                .topK(5)
                                .build()))
                .defaultOptions(ChatOptions.builder()
                        .model("mistral-large-latest")
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
