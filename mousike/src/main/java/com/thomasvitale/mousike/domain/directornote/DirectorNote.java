package com.thomasvitale.mousike.domain.directornote;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.UUID;

public record DirectorNote(@Id UUID id, @NotNull String movie, String sceneDescription, List<Marker> markers) {
}

