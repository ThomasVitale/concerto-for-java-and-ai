package com.thomasvitale.mousike.kontakt.instrument;

/**
 * One tag of one preset, to list a preset's tags one by one:
 * Kontakt's view joins them into a single string where
 * "Bowed Strings, Cello, Bowed Strings, Viola"
 * cannot be told apart from its parts.
 */
public record InstrumentTag(
        long instrument,
        String name
) {}
