package com.thomasvitale.mousike.assistant.internal;

import com.thomasvitale.mousike.assistant.Citation;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.decision.DecisionModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.postretrieval.document.DecisionDocumentFilter;
import org.springframework.ai.rag.postretrieval.document.DecisionDocumentReranker;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class NotebookRagAgent {

    private static final String SYSTEM_PROMPT = """
            You are the keeper of a composer's private notebook of notes about harmony, instruments, \
            orchestration, arrangement, styles and techniques for scoring film, games and theatre.
            The composer writes the music. You never write melodies, lyrics or full arrangements. \
            You surface and connect the composer's own ideas, suggest which notes apply to the scene at hand, \
            and keep answers short, concrete and musical. Mention chord progressions and instruments by name \
            when the notes do. Answer in Markdown with short paragraphs or a short list.""";

    private static final PromptTemplate CONTEXT_PROMPT = new PromptTemplate("""
            Notes from the composer's notebook:
            ---------------------
            {context}
            ---------------------
            Using only these notes, answer the question. Refer to the ideas as "your note" or "your notes". \
            Mention a gap, in one sentence, only when the question asks for something these notes do not cover; \
            otherwise end with the answer. Ignore notes that have nothing to do with the question, and do not \
            suggest notes to write or what they could say.
            Quote chords and numerals as the notes write them, never analyse them yourself or invent others, and \
            never write tool calls, links or images in your answer.

            Question: {query}""");

    private static final PromptTemplate EMPTY_CONTEXT_PROMPT = new PromptTemplate("""
            The composer's notebook has no notes close to this question. Say so in one sentence and nothing else: \
            no musical advice, and no suggestions of notes to write.

            Question: {query}""");

    private final ChatClient chatClient;

    NotebookRagAgent(ChatClient.Builder builder, VectorStore vectorStore, DecisionModel decisionModel) {
        RetrievalAugmentationAdvisor retrieval = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .similarityThreshold(0.45)
                        .topK(6)
                        .filterExpression(new FilterExpressionBuilder().eq("type", "note").build())
                        .build())
                .documentPostProcessors(
                        DecisionDocumentFilter.builder(decisionModel).build(),
                        DecisionDocumentReranker.builder(decisionModel).topK(4).build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .promptTemplate(CONTEXT_PROMPT)
                        .emptyContextPromptTemplate(EMPTY_CONTEXT_PROMPT)
                        .allowEmptyContext(true)
                        .build())
                .build();
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(retrieval)
                .build();
    }

    public NotebookAnswer answer(String question) {
        ChatClientResponse response = chatClient.prompt()
                .user(question)
                .call()
                .chatClientResponse();
        ChatResponse chatResponse = response.chatResponse();
        String text = chatResponse == null || chatResponse.getResult() == null ? "" : Objects.requireNonNullElse(chatResponse.getResult().getOutput().getText(), "");
        return new NotebookAnswer(text, citations(response.context()));
    }

    /** The notebook's answer and the notes it was built from. */
    public record NotebookAnswer(String markdown, List<Citation> citations) {}

    private static List<Citation> citations(Map<String, @Nullable Object> context) {
        if (!(context.get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT) instanceof List<?> documents)) {
            return List.of();
        }
        List<Citation> citations = new ArrayList<>();
        for (Object candidate : documents) {
            if (!(candidate instanceof Document document)) {
                continue;
            }
            Map<String, Object> metadata = document.getMetadata();
            String kind = String.valueOf(metadata.getOrDefault("kind", ""));
            UUID id = parseId(document.getId());
            citations.add(new Citation(id, kind, excerpt(Objects.requireNonNullElse(document.getText(), ""))));
        }
        return citations;
    }

    private static @Nullable UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String excerpt(String text) {
        int firstSentence = text.indexOf(". ");
        String body = firstSentence > 0 && firstSentence < 20 ? text.substring(firstSentence + 2) : text;
        return body.length() > 160 ? body.substring(0, 157) + "…" : body;
    }

}
