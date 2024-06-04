package com.thomasvitale.mousike.domain.directornote;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record Marker(@JsonPropertyDescription("Time in the format 'hh:mm:ss'") String time, String note) {
}
