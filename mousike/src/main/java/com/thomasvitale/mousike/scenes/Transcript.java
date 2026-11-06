package com.thomasvitale.mousike.scenes;

import java.time.Duration;

public record Transcript(
        String text,
        String model,
        Duration duration
) {}
