package com.thomasvitale.mousike.notebook.internal;

import com.thomasvitale.mousike.notebook.Note;
import com.thomasvitale.mousike.notebook.NoteDeleted;
import com.thomasvitale.mousike.notebook.NoteSaved;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
class NoteIndexer {

    static final String TYPE_NOTE = "note";

    private static final Logger logger = LoggerFactory.getLogger(NoteIndexer.class);

    private final NoteRepository noteRepository;
    private final VectorStore vectorStore;

    NoteIndexer(NoteRepository noteRepository, VectorStore vectorStore) {
        this.noteRepository = noteRepository;
        this.vectorStore = vectorStore;
    }

    @ApplicationModuleListener
    void on(NoteSaved event) {
        noteRepository.findById(event.noteId()).ifPresent(this::index);
    }

    @ApplicationModuleListener
    void on(NoteDeleted event) {
        vectorStore.delete(List.of(event.noteId().toString()));
    }

    void index(Note note) {
        Document document = Document.builder()
                .id(note.requiredId().toString())
                .text(note.indexText())
                .metadata(Map.of(
                        "type", TYPE_NOTE,
                        "kind", note.kind().name()
                ))
                .build();
        vectorStore.add(List.of(document));
        logger.debug("Indexed note {}", note.requiredId());
    }

}
