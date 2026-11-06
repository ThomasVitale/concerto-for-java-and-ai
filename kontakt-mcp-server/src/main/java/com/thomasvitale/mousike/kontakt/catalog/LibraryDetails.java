package com.thomasvitale.mousike.kontakt.catalog;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A library with where it lives and the manuals that describe
 * its banks, patches, and articulations.
 */
public record LibraryDetails(
        LibraryInfo library,
        @Nullable String folder,
        List<Manual> manuals
) {}
