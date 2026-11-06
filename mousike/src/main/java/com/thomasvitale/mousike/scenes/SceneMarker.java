package com.thomasvitale.mousike.scenes;

import org.springframework.data.relational.core.mapping.Table;

@Table
public record SceneMarker(String timeMarker, String comment) {}
