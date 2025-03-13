package com.thomasvitale.mousike.domain.directornote;

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

    public String transcribe(Resource audioResource) {
        return transcriptionService.transcribe(audioResource);
    }

    public ExtractedDirectorNote structure(String unstructuredDirectorNote) {
        return structuredDataExtractionService.extract(unstructuredDirectorNote, ExtractedDirectorNote.class);
    }

    public record ExtractedDirectorNote(String movie, String sceneDescription, List<Marker> markers) {}

}
