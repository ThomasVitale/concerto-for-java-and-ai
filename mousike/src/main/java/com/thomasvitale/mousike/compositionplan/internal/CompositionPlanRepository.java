package com.thomasvitale.mousike.compositionplan.internal;

import java.util.Optional;
import java.util.UUID;

import com.thomasvitale.mousike.compositionplan.CompositionPlan;

import org.springframework.data.repository.ListCrudRepository;

public interface CompositionPlanRepository extends ListCrudRepository<CompositionPlan, UUID> {

    Optional<CompositionPlan> findFirstBySceneIdOrderByCreatedAtDesc(UUID sceneId);

}
