package com.thomasvitale.mousike.domain.directornote;

import java.io.IOException;
import java.util.List;

import com.thomasvitale.mousike.ai.StructuredDataExtractionService;
import com.thomasvitale.mousike.ai.TranscriptionService;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class DirectorNoteService {

    private final DirectorNoteRepository directorNoteRepository;
    private final StructuredDataExtractionService structuredDataExtractionService;
    private final TranscriptionService transcriptionService;

    public DirectorNoteService(DirectorNoteRepository directorNoteRepository, StructuredDataExtractionService structuredDataExtractionService, TranscriptionService transcriptionService) {
        this.directorNoteRepository = directorNoteRepository;
        this.structuredDataExtractionService = structuredDataExtractionService;
        this.transcriptionService = transcriptionService;
    }

    public List<DirectorNote> findAll() {
        return directorNoteRepository.findAll();
    }

    public DirectorNote save(DirectorNote directorNote) {
        return directorNoteRepository.save(directorNote);
    }

    public String transcribe(Resource audioResource) throws IOException {
        return transcriptionService.transcribe(audioResource);
    }

    public DirectorNote structure(String unstructuredDirectorNote) {
        return structuredDataExtractionService.extract(unstructuredDirectorNote, DirectorNote.class);
    }

    public DirectorNote function(String unstructuredDirectorNote) {
        return structuredDataExtractionService.extract(unstructuredDirectorNote, DirectorNote.class, "saveDirectorNote");
    }

}
