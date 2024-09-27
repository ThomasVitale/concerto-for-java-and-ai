package com.thomasvitale.odai;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptionsBuilder;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class OdaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OdaiApplication.class, args);
    }

}

@RestController
class DemoController {

    private static final String USER_PROMPT = """
                Classify the type of the provided text.

                For example:

                Input: The celesta is a good fit for fantasy or mystery compositions, creating a sense of mystical and supernatural.
                Output: "INSTRUMENT"

                Input: The chord progression vi-IV-I-V is commonly used for epic scenes, such as action, battles, and scenes with pathos.
                Output: "ARRANGEMENT"

                Input: A chord progression for driving and triumphant scenes: i v VII IV.
                Output: "ARRANGEMENT"

                Input: They're taking the hobbits to Isengard! To Isengard! To Isengard!
                Output: "UNKNOWN"

                ---------------------
                TEXT:
                {text}
                ---------------------
                """;

    private final ChatClient chatClient;
    private final CompositionNoteRepository compositionNoteRepository;
    private final VectorStore vectorStore;

    DemoController(ChatClient.Builder chatClientBuilder, CompositionNoteRepository compositionNoteRepository, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultOptions(OllamaOptions.create()
                        .withFormat("json")
                        .withTemperature(0.0))
                .build();
        this.compositionNoteRepository = compositionNoteRepository;
        this.vectorStore = vectorStore;
    }

    @PostMapping("/classify")
    CompositionNote.Type classify(@RequestBody CompositionNote compositionNote) {
        return chatClient
                .prompt()
                .user(userSpec -> userSpec
                        .text(USER_PROMPT)
                        .param("text", compositionNote.content())
                )
                .call()
                .entity(CompositionNote.Type.class);
    }

    @PostMapping("/classify-and-save")
    CompositionNote classifyAndSave(@RequestBody CompositionNote compositionNote) {
        var compositionNoteType = chatClient.prompt()
                .user(userSpec -> userSpec
                        .text(USER_PROMPT)
                        .param("text", compositionNote.content())
                )
                .call()
                .entity(CompositionNote.Type.class);

        var classifiedCompositionNote = new CompositionNote(null, compositionNoteType, compositionNote.content());
        var savedCompositionNote = compositionNoteRepository.save(classifiedCompositionNote);

        vectorStore.add(List.of(new Document(savedCompositionNote.id().toString(), savedCompositionNote.type() + ". " + savedCompositionNote.content(),
                Map.of("type", savedCompositionNote.type())
        )));

        return savedCompositionNote;
    }

    @PostMapping("/semantic-search")
    List<CompositionNote> semanticSearch(@RequestBody CompositionSearch compositionSearch) {
        var similarDocuments = vectorStore.similaritySearch(SearchRequest.query(compositionSearch.query())
                .withSimilarityThreshold(0.85)
                .withTopK(3)
        );

        return compositionNoteRepository.findAllById(similarDocuments.stream()
                .map(Document::getId)
                .map(UUID::fromString)
                .toList());
    }

    @PostMapping("/ask")
    String ask(@RequestBody CompositionSearch compositionSearch) {
        return chatClient.prompt()
                .user(compositionSearch.query())
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults().withTopK(3)))
                .call()
                .content();
    }

}

interface CompositionNoteRepository extends ListCrudRepository<CompositionNote, UUID> {
    List<CompositionNote> findByType(CompositionNote.Type type);
}

record CompositionNote(@Id UUID id, Type type, String content) {
    enum Type {ARRANGEMENT, HARMONY, INSTRUMENT, MELODY, ORCHESTRATION, UNKNOWN}
}

record CompositionSearch(String query) {}
