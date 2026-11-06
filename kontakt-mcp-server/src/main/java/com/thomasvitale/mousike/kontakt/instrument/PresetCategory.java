package com.thomasvitale.mousike.kontakt.instrument;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The categories of Kontakt 8's Browser and the file formats each holds,
 * as Kontakt itself sorts them into its {@code p_sound_info_<category>}
 * tables. One-shots come from Expansions and have no format of their own here.
 */
public enum PresetCategory {

    INSTRUMENTS("Instruments", "nki", "nksn"),
    COMBINED("Combined", "nkm"),
    TOOLS("Tools", "nkt"),
    LEAP("Leap", "nkl"),
    LOOPS("Loops", "wav");

    private final String label;
    private final List<String> extensions;

    PresetCategory(String label, String... extensions) {
        this.label = label;
        this.extensions = List.of(extensions);
    }

    public String label() {
        return label;
    }

    public List<String> extensions() {
        return extensions;
    }

    /**
     * By the Browser's label, ignoring case, e.g. "instruments" or "Leap".
     */
    public static Optional<PresetCategory> of(String label) {
        return Arrays.stream(values()).filter(category -> category.label.equalsIgnoreCase(label.strip())).findFirst();
    }

    public static Optional<PresetCategory> ofExtension(String extension) {
        return Arrays.stream(values()).filter(category -> category.extensions.contains(extension)).findFirst();
    }

}
