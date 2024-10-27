package com.thomasvitale.mousike.domain.directornote;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;

public record DirectorNote(@Id UUID id, @NotNull String movie, String sceneDescription, List<Marker> markers) {
}

