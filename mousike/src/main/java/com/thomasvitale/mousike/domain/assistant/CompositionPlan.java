package com.thomasvitale.mousike.domain.assistant;

import java.util.List;

public record CompositionPlan(List<String> chordProgressions, List<String> compositionStrategySteps) {
}
