package com.thomasvitale.mousike.kontakt.catalog;

import java.net.URI;

/**
 * A document the vendor ships in the library's Documentation folder.
 *
 * @param path the file on disk, for a local reader such as Docling
 * @param url where this server serves the file
 */
public record Manual(
        String name,
        String path,
        URI url
) {}
