package com.thomasvitale.mousike.library;

import java.net.URI;

import org.jspecify.annotations.Nullable;

/**
 * A Kontakt preset as the library knows it.
 *
 * @param available whether its file is reachable now; false when its library sits on a drive that is not mounted
 * @param preview where the library streams the clip Kontakt's Browser plays to sample it, absent when the library
 *                ships none
 */
public record LibraryPreset(long id, String name, String product, boolean available, @Nullable URI preview) {
}
