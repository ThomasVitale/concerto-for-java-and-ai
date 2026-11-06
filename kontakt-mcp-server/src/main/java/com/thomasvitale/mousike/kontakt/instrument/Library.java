package com.thomasvitale.mousike.kontakt.instrument;

import org.jspecify.annotations.Nullable;

/**
 * An installed product, as the Browser's Library tiles show it,
 * and how many presets it contributes.
 *
 * @param author the Author of its presets; the Browser's Brand is not in this database,
 *               whose bank chains name no vendor
 * @param version the library's version as Native Access installed it, e.g. "1.5.0.0"
 * @param folder where Kontakt found the library; it may be on a drive not mounted now
 */
public record Library(
        String product,
        String author,
        int presets,
        @Nullable String version,
        @Nullable String folder
) {}
