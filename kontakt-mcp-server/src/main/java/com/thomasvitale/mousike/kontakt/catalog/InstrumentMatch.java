package com.thomasvitale.mousike.kontakt.catalog;

import com.thomasvitale.mousike.kontakt.instrument.Instrument;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * A search hit as the client should see it, in the words of Kontakt's Browser:
 * the preset name to load, the Product, Bank and Sub-bank it sits in,
 * its category and format, its Sound Type and Character tags,
 * and whether its file is reachable now (a library on an unmounted drive is not).
 *
 * @param category the Browser category, e.g. "Instruments" or "Combined"
 * @param format "Instrument", "Snapshot", "Multi", "Tool preset", "Leap kit" or "Loop"
 * @param soundTypes Type and Subtype pairs, e.g. "Bowed Strings, Cello"
 */
public record InstrumentMatch(
        long id,
        String name,
        String product,
        String author,
        @Nullable String bank,
        @Nullable String subBank,
        String category,
        String format,
        List<String> soundTypes,
        List<String> characters,
        boolean available
) {

    static InstrumentMatch of(Instrument instrument, List<String> soundTypes, List<String> characters) {
        return new InstrumentMatch(instrument.id(), instrument.name(), instrument.product(), instrument.authorOrBrand(),
                instrument.bank(), instrument.subbank(), instrument.category(), instrument.format(), soundTypes, characters,
                Files.isRegularFile(Path.of(instrument.fileName())));
    }

}
