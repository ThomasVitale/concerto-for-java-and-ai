package com.thomasvitale.mousike.kontakt.instrument;

/**
 * Headline numbers for the installed presets.
 */
public record InstrumentSummary(
        int presets,
        int libraries,
        int soundTypes,
        int characters
) {}
