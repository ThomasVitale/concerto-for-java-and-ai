package com.thomasvitale.mousike.compositionnote.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.thomasvitale.mousike.ai.StructuredDataExtractionService;
import com.thomasvitale.mousike.compositionnote.ai.ClassificationService;
import com.thomasvitale.mousike.compositionnote.ai.QuestionAnsweringService;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class CompositionNoteService {

    private final ClassificationService classificationService;
    private final CompositionNoteItemRepository compositionNoteItemRepository;
    private final CompositionNoteRepository compositionNoteRepository;
    private final QuestionAnsweringService questionAnsweringService;
    private final StructuredDataExtractionService structuredDataExtractionService;
    private final VectorStore vectorStore;

    public CompositionNoteService(ClassificationService classificationService, CompositionNoteItemRepository compositionNoteItemRepository, CompositionNoteRepository compositionNoteRepository, QuestionAnsweringService questionAnsweringService, StructuredDataExtractionService structuredDataExtractionService, VectorStore vectorStore) {
        this.classificationService = classificationService;
        this.compositionNoteItemRepository = compositionNoteItemRepository;
        this.compositionNoteRepository = compositionNoteRepository;
        this.questionAnsweringService = questionAnsweringService;
        this.structuredDataExtractionService = structuredDataExtractionService;
        this.vectorStore = vectorStore;
    }

    // SEARCH

    public List<CompositionNoteItem> findItemsViaLexicalSearch(String searchTerm, Pageable pageable) {
        return compositionNoteItemRepository.findByContentIgnoreCaseContaining(searchTerm, pageable).getContent();
    }

    public List<CompositionNoteItem> findItemsViaSemanticSearch(String searchTerm, Pageable pageable) {
        if (!StringUtils.hasText(searchTerm)) {
            return List.of();
        }

        var similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(searchTerm)
                        .similarityThreshold(0.50)
                        .topK(3)
                        .build()
        );

        if (CollectionUtils.isEmpty(similarDocuments)) {
            return List.of();
        }

        return compositionNoteItemRepository.findAllById(similarDocuments.stream()
                .map(Document::getId)
                .map(UUID::fromString)
                .toList());
    }

    public Optional<CompositionNote> findById(UUID id) {
        return compositionNoteRepository.findById(id);
    }

    public Optional<CompositionNoteItem> findItemById(UUID id) {
        return compositionNoteItemRepository.findById(id);
    }

    public long count() {
        return compositionNoteRepository.count();
    }

    // SAVE

    @Transactional
    public CompositionNote save(CompositionNote compositionNote) {
        if (compositionNote.getType() == null) {
            compositionNote = classify(compositionNote);
        }

        CompositionNote saved = compositionNoteRepository.save(new CompositionNote(compositionNote.getId(),
                compositionNote.getType(), compositionNote.getContent()));

        Assert.notNull(saved.getType(), "Composition note type must not be null");

        vectorStore.add(List.of(Document.builder()
                .id(saved.getId().toString())
                .text(saved.getType() + ". " + saved.getContent())
                .metadata("type", saved.getType())
                .build()
        ));
        return saved;
    }

    public CompositionNote classify(CompositionNote compositionNote) {
        var compositionNoteType = classificationService.classify(compositionNote.getContent(), CompositionNote.Type.class);
        return new CompositionNote(null, compositionNoteType, compositionNote.getContent());
    }

    public String answer(String question) {
        return questionAnsweringService.answer(question);
    }

    public ExtractedCompositionNote structure(String unstructuredCompositionNote) {
        return structuredDataExtractionService.extract(unstructuredCompositionNote, ExtractedCompositionNote.class);
    }

    public record ExtractedCompositionNote(CompositionNote.Type type, String content) {}

}
