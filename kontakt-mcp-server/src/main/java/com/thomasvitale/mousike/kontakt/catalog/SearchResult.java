package com.thomasvitale.mousike.kontakt.catalog;

import java.util.List;

/**
 * Matches plus a sentence the model can act on,
 * so an empty result is not a dead end.
 */
public record SearchResult(
        List<InstrumentMatch> matches,
        String note
) {}
