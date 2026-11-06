package com.thomasvitale.mousike.scenes.internal;

import java.util.List;
import java.util.UUID;

import com.thomasvitale.mousike.scenes.Scene;

import org.springframework.data.repository.ListCrudRepository;

public interface SceneRepository extends ListCrudRepository<Scene, UUID> {

    List<Scene> findAllByOrderByCreatedAtDesc();

}
