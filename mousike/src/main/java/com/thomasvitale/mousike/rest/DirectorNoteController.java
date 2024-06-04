package com.thomasvitale.mousike.rest;

import com.thomasvitale.mousike.domain.directornote.DirectorNote;
import com.thomasvitale.mousike.domain.directornote.DirectorNoteService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/notes/director")
class DirectorNoteController {

    private final DirectorNoteService directorNoteService;

    public DirectorNoteController(DirectorNoteService directorNoteService) {
        this.directorNoteService = directorNoteService;
    }

    @GetMapping
    List<DirectorNote> findAll() {
        return directorNoteService.findAll();
    }

    @PostMapping
    DirectorNote save(@RequestBody DirectorNote directorNote) {
        return directorNoteService.save(directorNote);
    }

    @PostMapping("/transcribe")
    UnstructuredDirectorNote transcribe(@RequestParam("audioFile") MultipartFile audioFile) throws IOException {
        Resource audioResource = new ByteArrayResource(audioFile.getBytes());
        return new UnstructuredDirectorNote(directorNoteService.transcribe(audioResource));
    }

    @PostMapping("/structure")
    DirectorNote structure(@RequestBody UnstructuredDirectorNote unstructuredDirectorNote) {
        return directorNoteService.structure(unstructuredDirectorNote.text());
    }

    @PostMapping("/function")
    DirectorNote function(@RequestBody UnstructuredDirectorNote unstructuredDirectorNote) {
        return directorNoteService.function(unstructuredDirectorNote.text());
    }

}

record UnstructuredDirectorNote(String text) {}

