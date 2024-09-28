package com.thomasvitale.mousike.domain.compositionnote;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.thomasvitale.mousike.ai.ClassificationService;
import com.thomasvitale.mousike.ai.QuestionAnsweringService;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class CompositionNoteService {

    private final CompositionNoteRepository compositionNoteRepository;
    private final ClassificationService classificationService;
    private final QuestionAnsweringService questionAnsweringService;
    private final VectorStore vectorStore;

    public CompositionNoteService(CompositionNoteRepository compositionNoteRepository, ClassificationService classificationService, QuestionAnsweringService questionAnsweringService, VectorStore vectorStore) {
        this.compositionNoteRepository = compositionNoteRepository;
        this.classificationService = classificationService;
        this.questionAnsweringService = questionAnsweringService;
        this.vectorStore = vectorStore;
    }

    public long count() {
        return compositionNoteRepository.count();
    }

    public List<CompositionNote> findAll() {
        return compositionNoteRepository.findAll();
    }

    public List<CompositionNote> findAllByType(CompositionNote.Type type) {
        return compositionNoteRepository.findByType(type);
    }

    public Optional<CompositionNote> findById(UUID uuid) {
        return compositionNoteRepository.findById(uuid);
    }

    public List<CompositionNote.Type> getTypes() {
        return List.of(CompositionNote.Type.values());
    }

    public CompositionNote save(CompositionNote compositionNote) {
        var savedCompositionNote =  compositionNoteRepository.save(compositionNote);
        vectorStore.add(List.of(new Document(savedCompositionNote.getId().toString(), savedCompositionNote.getType() + ". " + savedCompositionNote.getContent(),
                Map.of("type", savedCompositionNote.getType())
        )));
        return savedCompositionNote;
    }

    public CompositionNote classify(CompositionNote compositionNote) {
        var compositionNoteType = classificationService.classify(compositionNote.getContent(), CompositionNote.Type.class);
        var classifiedCompositionNote = new CompositionNote(null, compositionNoteType, compositionNote.getContent());
        var savedCompositionNote = compositionNoteRepository.save(classifiedCompositionNote);

        vectorStore.add(List.of(new Document(savedCompositionNote.getId().toString(), savedCompositionNote.getType().toString() + ". " + savedCompositionNote.getContent(),
                Map.of("type", savedCompositionNote.getType().toString())
        )));

        return savedCompositionNote;
    }

    public List<CompositionNote> keywordSearch(String keyword) {
        return compositionNoteRepository.findByContentIgnoreCaseContaining(keyword);
    }

    public List<CompositionNote> semanticSearch(String query) {
        var similarDocuments = vectorStore.similaritySearch(
                SearchRequest.query(query).withTopK(3)
        );

        return compositionNoteRepository.findAllById(similarDocuments.stream()
                .map(Document::getId)
                .map(UUID::fromString)
                .toList());
    }

    public String ask(String question) {
        return questionAnsweringService.answer(question);
    }

}
