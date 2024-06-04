package com.thomasvitale.mousike.rest;

import com.thomasvitale.mousike.domain.assistant.ComposerAssistantService;
import com.thomasvitale.mousike.domain.assistant.CompositionPlan;
import com.thomasvitale.mousike.domain.assistant.SceneToScore;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assistant")
public class AssistantController {

    private final ComposerAssistantService composerAssistantService;

    public AssistantController(ComposerAssistantService composerAssistantService) {
        this.composerAssistantService = composerAssistantService;
    }

    @PostMapping
    CompositionPlan plan(@RequestBody SceneToScore sceneToScore) {
        return composerAssistantService.plan(sceneToScore);
    }
}
