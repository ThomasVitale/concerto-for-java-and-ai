package com.thomasvitale.mousike.kontakt.instrument;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * One row of Kontakt's {@code v_sound_info} view: a preset as the Browser lists it (an instrument, snapshot,
 * multi, Tool preset, Leap kit, or loop). Read-only. The view belongs to Kontakt.
 *
 * @param author the preset's Author, as the Browser's Info pane shows it
 * @param brand always "Native Instruments" in practice: the bank chains name no vendor
 * @param bank with {@code subbank}, the Browser's Bank filter: Product, Bank, Sub-bank
 * @param type the Sound Types joined into one string, e.g. "Bowed Strings, Cello"
 * @param character the Character tags joined into one string, e.g. "Sample-based, Evolving"
 * @param comment the optional comment embedded in the preset, often shared by a whole library
 * @param fileName the absolute path of the preset's file
 * @param fileExt nki, nksn, nkm, nkt, nkl or wav
 */
@Table("v_sound_info")
public record Instrument(
        @Id long id,
        String name,
        @Nullable String author,
        String brand,
        String product,
        @Nullable String bank,
        @Nullable String subbank,
        @Nullable String type,
        @Nullable String character,
        @Nullable String comment,
        String fileName,
        @Nullable String fileExt
) {

    /**
     * The preset's Author when Kontakt knows it, else its brand.
     */
    public String authorOrBrand() {
        return author == null || author.isBlank() ? brand : author;
    }

    /**
     * The Browser category the preset is listed under,
     * e.g. "Instruments", or "Other" for an unknown format.
     */
    public String category() {
        return PresetCategory.ofExtension(fileExt == null ? "" : fileExt).map(PresetCategory::label).orElse("Other");
    }

    /**
     * The file format in the manual's words.
     */
    public String format() {
        return switch (fileExt == null ? "" : fileExt) {
            case "nki" -> "Instrument";
            case "nksn" -> "Snapshot";
            case "nkm" -> "Multi";
            case "nkt" -> "Tool preset";
            case "nkl" -> "Leap kit";
            case "wav" -> "Loop";
            case "" -> "Unknown";
            default -> fileExt;
        };
    }

}
