package com.thomasvitale.mousike.kontakt.catalog;

import org.jspecify.annotations.Nullable;

/**
 * Everything the database knows about one entry, plus its audio preview, its library, and the library's manuals.
 *
 * @param comment the vendor's note, often shared by the whole library
 * @param file the entry's file on disk
 * @param preview null when the library ships no preview for the entry
 */
public record InstrumentDetails(
        InstrumentMatch instrument,
        @Nullable String comment,
        String file,
        @Nullable Preview preview,
        LibraryDetails library
) {}
