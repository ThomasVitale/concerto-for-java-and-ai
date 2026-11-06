package com.thomasvitale.mousike.notebook;

import com.thomasvitale.mousike.notebook.Classification.Classified;
import com.thomasvitale.mousike.notebook.Classification.NeedsReview;
import com.thomasvitale.mousike.notebook.internal.ClassificationPolicy;
import com.thomasvitale.mousike.notebook.internal.NoteRepository;
import com.thomasvitale.mousike.notebook.internal.NoteTagger;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.UUID;

@Service
public class NoteService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private static final double SIMILARITY_THRESHOLD = 0.45;

    private final ApplicationEventPublisher eventPublisher;
    private final ClassificationPolicy classificationPolicy;
    private final NoteRepository noteRepository;
    private final NoteTagger noteTagger;
    private final TransactionTemplate transactionTemplate;
    private final VectorStore vectorStore;

    NoteService(NoteRepository noteRepository,
                NoteTagger noteTagger,
                VectorStore vectorStore,
                ApplicationEventPublisher eventPublisher,
                TransactionTemplate transactionTemplate,
                ClassificationPolicy classificationPolicy
    ) {
        this.noteRepository = noteRepository;
        this.noteTagger = noteTagger;
        this.vectorStore = vectorStore;
        this.eventPublisher = eventPublisher;
        this.transactionTemplate = transactionTemplate;
        this.classificationPolicy = classificationPolicy;
    }

    public Classification create(String content) {
        NoteTags tags = noteTagger.tag(content.strip());
        return classified(tags, () -> Note.create(content.strip(), tags));
    }

    public Note create(String content, NoteKind kind) {
        return save(Note.create(content.strip(), kind));
    }

    public Classification revise(UUID id, String content) {
        Note note = noteRepository.findById(id).orElseThrow(() -> new NoteNotFoundException(id));
        NoteTags tags = noteTagger.tag(content.strip());
        return classified(tags, () -> note.revised(content.strip(), tags));
    }

    public Note revise(UUID id, String content, NoteKind kind) {
        Note note = noteRepository.findById(id).orElseThrow(() -> new NoteNotFoundException(id));
        return save(note.revised(content.strip(), kind));
    }

    /** Saves the note when the model's choice stands on its own; otherwise a person has to choose, and nothing is saved. */
    private Classification classified(NoteTags tags, Supplier<Note> note) {
        return classificationPolicy.needsReview(tags)
                ? new NeedsReview(tags.ranked().stream().limit(2).toList())
                : new Classified(save(note.get()));
    }

    private Note save(Note note) {
        return Objects.requireNonNull(transactionTemplate.execute(status -> {
            Note saved = noteRepository.save(note);
            eventPublisher.publishEvent(new NoteSaved(saved.requiredId()));
            return saved;
        }));
    }

    @Transactional
    public void delete(UUID id) {
        noteRepository.deleteById(id);
        eventPublisher.publishEvent(new NoteDeleted(id));
    }

    public List<Note> all() {
        return noteRepository.findAll();
    }

    public long count() {
        return noteRepository.count();
    }

    public List<KindCount> countByKind() {
        return noteRepository.countByKind();
    }

    public List<Note> browse(@Nullable String text, @Nullable NoteKind kind, int limit) {
        Pageable page = PageRequest.of(0, limit, NEWEST_FIRST);
        boolean hasText = text != null && !text.isBlank();
        if (kind != null && hasText) {
            return noteRepository.findByKindAndContentContainingIgnoreCase(kind, text.strip(), page);
        }
        if (kind != null) {
            return noteRepository.findByKind(kind, page);
        }
        if (hasText) {
            return noteRepository.findByContentContainingIgnoreCase(text.strip(), page);
        }
        return noteRepository.findAll(page).getContent();
    }

    public List<ScoredNote> searchByMeaning(String query, @Nullable NoteKind kind, int limit) {
        if (query.isBlank()) {
            return List.of();
        }
        FilterExpressionBuilder filter = new FilterExpressionBuilder();
        Filter.Expression expression = kind == null
                ? filter.eq("type", "note").build()
                : filter.and(filter.eq("type", "note"), filter.eq("kind", kind.name())).build();
        List<Document> hits = vectorStore.similaritySearch(SearchRequest.builder()
                .query(query.strip())
                .topK(limit)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .filterExpression(expression)
                .build());
        if (hits.isEmpty()) {
            return List.of();
        }
        Map<UUID, Double> scores = new LinkedHashMap<>();
        hits.forEach(hit -> scores.put(UUID.fromString(hit.getId()), hit.getScore() == null ? 0.0 : hit.getScore()));
        Map<UUID, Note> byId = new LinkedHashMap<>();
        noteRepository.findAllById(scores.keySet()).forEach(note -> byId.put(note.requiredId(), note));
        return scores.entrySet().stream()
                .map(entry -> byId.containsKey(entry.getKey()) ? new ScoredNote(byId.get(entry.getKey()), entry.getValue()) : null)
                .filter(Objects::nonNull)
                .toList();
    }

}
