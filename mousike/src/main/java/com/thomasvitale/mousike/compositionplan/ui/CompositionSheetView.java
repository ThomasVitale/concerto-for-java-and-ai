package com.thomasvitale.mousike.compositionplan.ui;

import com.thomasvitale.mousike.compositionplan.CompositionPlan;
import com.thomasvitale.mousike.compositionplan.CompositionSheet;
import com.thomasvitale.mousike.chords.MidiOutput;
import com.thomasvitale.mousike.chords.ChordProgressionService;
import com.thomasvitale.mousike.chords.ui.ChordProgressionCard;
import com.thomasvitale.mousike.ui.ModelBadge;
import com.thomasvitale.mousike.ui.StaffDivider;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

import org.springframework.core.task.AsyncTaskExecutor;

/**
 * A composition plan as the composer reads it before sitting down to write: concept, palette, harmony to hear, and the
 * arc along the director's markers.
 */
class CompositionSheetView extends Div {

    CompositionSheetView(CompositionPlan plan, ChordProgressionService progressions, MidiOutput midiOutput, AsyncTaskExecutor executor) {
        addClassName("composition-sheet");
        CompositionSheet sheet = plan.plan();

        Paragraph concept = new Paragraph(sheet.concept().isBlank() ? "No concept written." : sheet.concept());
        concept.addClassName("composition-concept");

        add(concept,
                section("Instruments", "What to load"), palette(sheet),
                section("Chord progressions", "Your notes first, then suggestions"), harmony(sheet, progressions, midiOutput, executor),
                section("Moment by moment", "Along the director's markers"), arc(sheet),
                provenance(plan));
    }

    private static Div palette(CompositionSheet sheet) {
        Div palette = new Div();
        palette.addClassName("composition-palette");
        sheet.palette().forEach(entry -> palette.add(paletteRow(entry)));
        if (sheet.palette().isEmpty()) {
            palette.add(muted("No instruments named."));
        }
        return palette;
    }

    private static Div paletteRow(CompositionSheet.PaletteEntry entry) {
        Span role = new Span(entry.role());
        role.addClassName("composition-role");
        Span instrument = new Span(entry.instrument());
        instrument.addClassName("composition-instrument");
        Div copy = new Div(instrument);
        copy.addClassName("composition-palette-copy");
        if (entry.preset() != null) {
            Span preset = new Span(entry.library() == null ? entry.preset() : entry.preset() + " (" + entry.library() + ")");
            preset.addClassName("composition-preset");
            preset.getElement().setAttribute("title", "Kontakt preset to load");
            Div line = new Div(preset);
            line.addClassName("composition-preset-line");
            if (entry.preview() != null) {
                line.add(new PresetSample(entry.preset(), entry.preview()));
            }
            copy.add(line);
        }
        if (!entry.why().isBlank()) {
            Span why = new Span(entry.why());
            why.addClassName("composition-why");
            copy.add(why);
        }
        Div row = new Div(role, copy);
        row.addClassName("composition-palette-row");
        return row;
    }

    private static Div harmony(CompositionSheet sheet, ChordProgressionService progressions, MidiOutput midiOutput, AsyncTaskExecutor executor) {
        Div harmony = new Div();
        harmony.addClassName("composition-harmony");
        for (CompositionSheet.HarmonicIdea idea : sheet.harmony()) {
            Div card = new Div();
            card.addClassName("composition-idea");
            card.add(muted(idea.fromNotes() ? "From your notes" : "Suggested"));
            if (!idea.why().isBlank()) {
                Paragraph why = new Paragraph(idea.why());
                why.addClassName("composition-why");
                card.add(why);
            }
            if (idea.progressionId() != null) {
                progressions.findById(idea.progressionId()).ifPresent(progression -> card.add(new ChordProgressionCard(progression, midiOutput, executor)));
            }
            else {
                Span chords = new Span(idea.name() + ": " + String.join(" ", idea.chords()) + (idea.key().isBlank() ? "" : " in " + idea.key()));
                chords.addClassName("studio-mono");
                card.add(chords, muted("Couldn't play this one."));
            }
            harmony.add(card);
        }
        if (sheet.harmony().isEmpty()) {
            harmony.add(muted("No progressions for this scene."));
        }
        return harmony;
    }

    private static Div arc(CompositionSheet sheet) {
        Div timeline = new Div();
        timeline.addClassName("scene-timeline");
        for (CompositionSheet.OrchestrationStep step : sheet.arc()) {
            Span time = new Span(step.timeMarker());
            time.addClassNames("studio-timecode", "marker-time-mark");
            Div copy = new Div(new Span(step.instruction()));
            copy.addClassName("composition-step");
            if (!step.instruments().isEmpty()) {
                Div chips = new Div();
                chips.addClassName("composition-chips");
                step.instruments().forEach(name -> {
                    Span chip = new Span(name);
                    chip.addClassName("composition-chip");
                    chips.add(chip);
                });
                copy.add(chips);
            }
            Div row = new Div(time, copy);
            row.addClassName("timeline-row");
            timeline.add(row);
        }
        if (sheet.arc().isEmpty()) {
            timeline.add(muted("Nothing planned for these moments."));
        }
        return timeline;
    }

    private static Div provenance(CompositionPlan plan) {
        CompositionSheet sheet = plan.plan();
        Span split = new Span("Gathered with tools in %s, structured in %s".formatted(ModelBadge.duration(sheet.gatherDuration()),
                ModelBadge.duration(sheet.structureDuration())));
        split.addClassName("evidence-verdict");
        Div provenance = new Div(new ModelBadge(plan.model() == null ? "chat model" : plan.model(),
                sheet.gatherDuration().plus(sheet.structureDuration())), split);
        provenance.addClassName("evidence-provenance");
        return provenance;
    }

    private static Div section(String title, String hint) {
        H3 heading = new H3(title);
        heading.addClassName("composition-section-title");
        Span hintSpan = new Span(hint);
        hintSpan.addClassName("composition-section-hint");
        Div section = new Div(new StaffDivider(), heading, hintSpan);
        section.addClassName("composition-section");
        return section;
    }

    private static Span muted(String text) {
        Span span = new Span(text);
        span.addClassName("scene-meta");
        return span;
    }

}
