package com.thomasvitale.mousike.directornote.domain;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record Marker(@JsonPropertyDescription("Time in the format 'hh:mm:ss'") String time, String note) {
}
