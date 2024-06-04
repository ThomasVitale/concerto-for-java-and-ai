package com.thomasvitale.mousike.rest;

import com.thomasvitale.mousike.ai.ClassificationService;
import com.thomasvitale.mousike.domain.compositionnote.CompositionNote;
import com.thomasvitale.mousike.domain.compositionnote.CompositionNoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notes/composition")
class CompositionNoteController {

    private final CompositionNoteService compositionNoteService;
    private final ClassificationService classificationService;

    public CompositionNoteController(CompositionNoteService compositionNoteService, ClassificationService classificationService) {
        this.compositionNoteService = compositionNoteService;
        this.classificationService = classificationService;
    }

    @GetMapping
    List<CompositionNote> findAll(@RequestParam(required = false) CompositionNote.Type type) {
        if (type != null) {
            return compositionNoteService.findAllByType(type);
        }
        return compositionNoteService.findAll();
    }

    @PostMapping
    CompositionNote save(@RequestBody CompositionNote compositionNote) {
        return compositionNoteService.save(compositionNote);
    }

    @PostMapping("/classify")
    CompositionNote classifyWithPromptAndSave(@RequestBody CompositionNote compositionNote) {
        return compositionNoteService.classify(compositionNote);
    }

    @PostMapping("/classify/exp")
    String classifyWithPromptInjection(@RequestBody CompositionNote compositionNote) {
        return classificationService.classify(compositionNote.getContent(), String.class);
    }

    @PostMapping("/semantic-search")
    List<CompositionNote> semanticSearch(@RequestBody CompositionSearch compositionSearch) {
        return compositionNoteService.semanticSearch(compositionSearch.query());
    }

    @PostMapping("/ask")
    CompositionAnswer ask(@RequestBody CompositionSearch compositionSearch) {
        return new CompositionAnswer(compositionNoteService.ask(compositionSearch.query()));
    }
}

record CompositionSearch(String query) {}
record CompositionAnswer(String answer) {}
