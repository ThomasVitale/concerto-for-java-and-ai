package com.thomasvitale.mousike.kontakt.catalog;

import java.net.URI;

/**
 * The short Ogg Vorbis recording Kontakt's browser plays for an entry.
 *
 * @param path the file on disk
 * @param url where this server streams it
 */
public record Preview(
        String path,
        URI url
) {}
