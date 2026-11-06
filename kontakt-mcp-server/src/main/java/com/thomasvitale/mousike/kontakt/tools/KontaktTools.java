package com.thomasvitale.mousike.kontakt.tools;

import com.thomasvitale.mousike.kontakt.catalog.Catalog;
import com.thomasvitale.mousike.kontakt.catalog.InstrumentDetails;
import com.thomasvitale.mousike.kontakt.catalog.LibraryInfo;
import com.thomasvitale.mousike.kontakt.catalog.SearchResult;
import com.thomasvitale.mousike.kontakt.config.KontaktProperties;
import com.thomasvitale.mousike.kontakt.instrument.InstrumentRepository;
import com.thomasvitale.mousike.kontakt.instrument.InstrumentSummary;
import com.thomasvitale.mousike.kontakt.instrument.Tag;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * What the server exposes over MCP. Tool names use snake_case like most MCP servers. The descriptions use the
 * words of Kontakt 8's Browser (preset, Product, Bank, Sound Type, Character) and are written for a model planning
 * a cue, which needs preset names it can tell the composer to load.
 */
@Component
class KontaktTools {

    private final Catalog catalog;
    private final InstrumentRepository instruments;
    private final KontaktProperties properties;

    KontaktTools(Catalog catalog, InstrumentRepository instruments, KontaktProperties properties) {
        this.catalog = catalog;
        this.instruments = instruments;
        this.properties = properties;
    }

    @McpTool(name = "list_libraries", description = """
            The libraries installed in the composer's Kontakt (the Browser's Library tiles), with author, version, \
            how many presets each holds and whether it is available now (false when its drive is not mounted). Use \
            it to know which products you may name.""")
    List<LibraryInfo> listLibraries() {
        return catalog.libraries();
    }

    @McpTool(name = "search_instruments", description = """
            Find Kontakt presets by words from their name, author, product, bank, Sound Type, Character or comment, \
            e.g. 'solo cello legato', 'low brass', 'taiko', 'evolving pad'. Returns the preset id and name to load, \
            its product, bank and sub-bank, its Browser category and format (an Instrument, or a Snapshot of one, a \
            Multi, ...), its Sound Types and Characters, and whether it is available now. Filter by Sound Type \
            (e.g. 'Brass', 'Bowed Strings, Cello') or Character (e.g. 'Evolving', 'Dark') when the query alone is \
            too broad. Searches every category but Tools, and only what the composer can load now, unless told \
            otherwise.""")
    SearchResult searchInstruments(
            @McpToolParam(description = "Words to look for, e.g. 'solo cello legato'") String query,
            @McpToolParam(description = "Only presets with a Sound Type containing this text, e.g. 'Brass'", required = false) @Nullable String soundType,
            @McpToolParam(description = "Only presets with a Character containing this text, e.g. 'Dark'", required = false) @Nullable String character,
            @McpToolParam(description = "Only this Browser category: Instruments, Combined (multis and Tool-enhanced presets), Tools (MIDI effects, no sound), Leap or Loops", required = false) @Nullable String category,
            @McpToolParam(description = "Maximum number of results, 1 to 50, default 12", required = false) @Nullable Integer limit,
            @McpToolParam(description = "Only presets the composer can load now, default true; false also searches libraries on drives that are not mounted", required = false) @Nullable Boolean availableOnly
    ) {
        return catalog.search(query, soundType, character, category, limit, !Boolean.FALSE.equals(availableOnly));
    }

    @McpTool(name = "get_instrument", description = """
            Everything known about one preset, by the id search_instruments returned: the comment embedded in it, \
            its file, its audio preview, its library's folder and the manuals the library ships (file path and \
            URL). Use it when a name and its tags leave you unsure what the preset does.""")
    InstrumentDetails getInstrument(@McpToolParam(description = "The preset id from search_instruments") long id) {
        return catalog.instrument(id).orElseThrow(() ->
                new IllegalArgumentException("No Kontakt preset has id %d. Use an id from search_instruments.".formatted(id)));
    }

    @McpTool(name = "list_sound_types", description = """
            Kontakt's Sound Types as Type and Subtype pairs ('Bowed Strings, Cello', 'Brass, Ensemble', 'Synth Pad', \
            ...) with how many presets carry each. Use the exact text, or just the Type, as the soundType filter of \
            search_instruments.""")
    List<Tag> listSoundTypes() {
        return instruments.soundTypes();
    }

    @McpTool(name = "list_characters", description = """
            Kontakt's Character tags ('Acoustic', 'Evolving', 'Dark', 'Huge', ...) with how many presets carry each. \
            Use the exact text as the character filter of search_instruments.""")
    List<Tag> listCharacters() {
        return instruments.characters();
    }

    @McpResource(uri = "kontakt://summary", name = "Kontakt summary", description = "Headline numbers of the installed Kontakt presets and where they come from.")
    String summary() {
        InstrumentSummary summary = instruments.summary();
        long available = catalog.libraries().stream().filter(LibraryInfo::available).count();
        return "%d presets from %d libraries (%d available now), with %d Sound Types and %d Characters. Source: %s".formatted(summary.presets(),
                summary.libraries(), available, summary.soundTypes(), summary.characters(), properties.database());
    }

}
