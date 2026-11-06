package com.thomasvitale.mousike.kontakt.instrument;

/**
 * An NKS tag as Kontakt's Browser filters by it,
 * a Sound Type (e.g. "Brass, Ensemble", Type and Subtype) or a
 * Character (e.g. "Evolving"), and how many presets carry it.
 */
public record Tag(
        String name,
        int presets
) {}
