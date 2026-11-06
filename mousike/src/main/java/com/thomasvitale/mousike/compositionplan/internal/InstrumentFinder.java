package com.thomasvitale.mousike.compositionplan.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.thomasvitale.mousike.agent.ProgressToolCallback;
import com.thomasvitale.mousike.compositionplan.CompositionSheet;
import com.thomasvitale.mousike.compositionplan.NotebookTools;
import com.thomasvitale.mousike.library.InstrumentLibrary;
import com.thomasvitale.mousike.library.LibraryPreset;
import com.thomasvitale.mousike.scenes.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * The plan's second step, the agentic one: the model searches the composer's notes and the instrument library as often as it
 * needs and writes a list, then a call without tools turns the list into data.
 */
@Component
public class InstrumentFinder {

    private static final Logger log = LoggerFactory.getLogger(InstrumentFinder.class);

    private final ChatClient gatherer;
    private final ChatClient structurer;
    private final NotebookTools notebookTools;
    private final InstrumentLibrary instrumentLibrary;

    InstrumentFinder(ChatClient.Builder builder, NotebookTools notebookTools, InstrumentLibrary instrumentLibrary) {
        this.gatherer = builder.build();
        this.structurer = PlanDraft.structured(builder, 0.0).build();
        this.notebookTools = notebookTools;
        this.instrumentLibrary = instrumentLibrary;
    }

    public List<CompositionSheet.PaletteEntry> find(Scene scene, Consumer<String> progress) {
        ToolCallback[] libraryTools = instrumentLibrary.toolCallbacks();
        boolean libraryOnline = libraryTools.length > 0;
        if (!libraryOnline) {
            progress.accept("The instrument library is offline: planning without preset names");
        }
        String list = gather(scene, libraryTools, progress);
        log.debug("Instruments for '{}':\n{}", scene.title(), list);
        PlanDraft.Palette draft = structurer.prompt()
                .system(PlanPrompts.PALETTE)
                .user(list)
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .call()
                .entity(PlanDraft.Palette.class);
        return palette(draft == null ? List.of() : PlanDraft.nullSafe(draft.instruments()), libraryOnline, progress);
    }

    private String gather(Scene scene, ToolCallback[] libraryTools, Consumer<String> progress) {
        Map<String, String> answers = ProgressToolCallback.newMemory();
        Object[] tools = Stream.concat(Arrays.stream(ToolCallbacks.from(notebookTools)), Arrays.stream(libraryTools))
                .map(tool -> new ProgressToolCallback(tool, progress, answers))
                .toArray();
        ChatResponse gathered = gatherer.prompt()
                .system(libraryTools.length > 0 ? PlanPrompts.INSTRUMENTS : PlanPrompts.INSTRUMENTS_LIBRARY_OFFLINE)
                .user(PlanPrompts.describe(scene))
                .tools(tools)
                .call()
                .chatResponse();
        return gathered == null || gathered.getResult() == null ? "" : Objects.requireNonNullElse(gathered.getResult().getOutput().getText(), "");
    }

    /** The name, the library and the clip come from the library itself, not from what the model wrote. */
    private List<CompositionSheet.PaletteEntry> palette(List<PlanDraft.Instrument> drafted, boolean libraryOnline, Consumer<String> progress) {
        List<PlanDraft.Instrument> instruments = drafted.stream().filter(entry -> !PlanDraft.blank(entry.instrument())).toList();
        if (libraryOnline && instruments.stream().anyMatch(entry -> entry.presetId() > 0)) {
            progress.accept("Finding the presets in your library");
        }
        return instruments.stream()
                .map(entry -> {
                    Optional<LibraryPreset> preset = entry.presetId() > 0 && libraryOnline ? instrumentLibrary.findById(entry.presetId()) : Optional.empty();
                    return new CompositionSheet.PaletteEntry(PlanDraft.orElse(entry.role(), "instrument"), entry.instrument().strip(),
                            preset.map(LibraryPreset::name).orElse(null), preset.map(LibraryPreset::product).orElse(null),
                            PlanDraft.orElse(entry.why(), ""), preset.map(LibraryPreset::preview).orElse(null));
                })
                .toList();
    }

}
