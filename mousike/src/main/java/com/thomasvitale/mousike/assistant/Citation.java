package com.thomasvitale.mousike.assistant;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record Citation(
        @Nullable UUID id,
        String kind,
        String excerpt
) {}
