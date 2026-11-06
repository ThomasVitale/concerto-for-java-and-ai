package com.thomasvitale.mousike.chords;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * The one tool a chat model may call to make sound: it renders a chord progression as MIDI.
 * The model chooses the numerals and the key; Java does the music theory and guarantees a playable file.
 * The result returns directly to the composer ({@code returnDirect}): the model's words travel in the call, the
 * tool-calling loop ends after one round, and a small local model never gets to call the tool again.
 */
@Component
public class ChordProgressionTools {

    private static final Logger log = LoggerFactory.getLogger(ChordProgressionTools.class);

    public static final String TOOL_NAME = "playChordProgression";

    private final ChordProgressionService chordProgressionService;

    public ChordProgressionTools(ChordProgressionService chordProgressionService) {
        this.chordProgressionService = chordProgressionService;
    }

    @Tool(name = TOOL_NAME, returnDirect = true, description = """
            Hand a chord progression to the composer to play, together with your words. Use it when they want to \
            hear a progression they named or one from this conversation, also in another key. Give it as Roman numerals and a key: the chords for that key are worked out for you. To hear it in another key, call this again with \
            the same numerals and the new key.""")
    public ChordProgressionResult playChordProgression(
            @ToolParam(description = "Short title without the key, e.g. 'Chase'") String title,
            @ToolParam(description = "The key, e.g. 'D minor', 'F# major'") String key,
            @ToolParam(description = "Roman numerals in order, e.g. ['i', 'VI', 'III', 'VII'] or ['Imaj7', 'vi7', 'ii9', 'V7sus4']; chord symbols only when the composer names chords") List<String> numerals,
            @ToolParam(description = "One or two sentences for the composer: why it fits and which of their notes it comes from. Do not spell out the chords, they are shown") String whyItFits) {
        List<String> symbols = numerals.stream().flatMap(numeral -> Arrays.stream(numeral.split("[\\s,–-]+"))).filter(numeral -> !numeral.isEmpty()).toList();
        Key parsedKey = Key.parse(key);
        List<String> chords = ChordParser.parseAll(symbols, parsedKey).stream().map(chord -> chord.symbol(parsedKey)).toList();
        log.debug("Model asked for '{}' in {}: {} is {}", title, key, symbols, chords);
        ChordProgression chordProgression = chordProgressionService.render(title, key, chords, ChordProgressionService.DEFAULT_TEMPO,
                ChordProgressionService.DEFAULT_BEATS_PER_CHORD);
        return new ChordProgressionResult(chordProgression.requiredId(), chordProgression.title(), chordProgression.keySignature(), symbols, chords, whyItFits.strip());
    }

    /** What the UI needs to show it and the conversation needs to play it again in another key. */
    public record ChordProgressionResult(UUID progressionId, String title, String key, List<String> numerals, List<String> chords, String whyItFits) {}

}
