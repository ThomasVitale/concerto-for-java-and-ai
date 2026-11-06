package com.thomasvitale.mousike.scenes;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public record SceneDraft(
        String production,
        String title,
        String description,
        Medium medium,
        @Nullable String genre,
        @Nullable String transcript,
        List<SceneMarker> markers,
        String model,
        Duration duration
) {}
