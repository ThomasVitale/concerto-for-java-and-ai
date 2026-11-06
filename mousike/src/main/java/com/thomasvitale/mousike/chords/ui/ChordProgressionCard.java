package com.thomasvitale.mousike.chords.ui;

import java.util.concurrent.CompletableFuture;

import com.thomasvitale.mousike.chords.MidiOutput;
import com.thomasvitale.mousike.chords.ChordProgression;
import com.thomasvitale.mousike.chords.ui.internal.ChordProgressionPlayer;
import com.thomasvitale.mousike.ui.ErrorToast;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

import org.springframework.core.task.AsyncTaskExecutor;

/**
 * A progression with its player and, when a MIDI port is online, a button that sends it to Logic Pro.
 */
public class ChordProgressionCard extends Div {

    public ChordProgressionCard(ChordProgression progression, MidiOutput midiOutput, AsyncTaskExecutor executor) {
        addClassName("progression-card");

        Span title = new Span(progression.title());
        title.addClassName("progression-title");
        Span meta = new Span("%s, %d bpm, %d beats per chord".formatted(progression.keySignature(), progression.tempoBpm(), progression.beatsPerChord()));
        meta.addClassName("progression-meta");
        Div head = new Div(title, meta);
        head.addClassName("progression-head");

        Span voicing = new Span(String.join("   ", progression.voicingList()));
        voicing.addClassName("progression-voicing");
        voicing.getElement().setAttribute("title", "Voiced notes, lowest first");

        add(head, new ChordProgressionPlayer(progression), voicing);

        if (midiOutput.isAvailable()) {
            Button send = new Button("Send to Logic Pro", VaadinIcon.EXTERNAL_LINK.create());
            send.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
            send.addClickListener(e -> {
                UI ui = UI.getCurrent();
                send.setEnabled(false);
                send.setText("Playing on " + midiOutput.portName() + "…");
                CompletableFuture.runAsync(() -> midiOutput.play(progression.midi()), executor)
                        .whenComplete((v, failure) -> ui.access(() -> {
                            send.setEnabled(true);
                            send.setText("Send to Logic Pro");
                            if (failure != null) {
                                ErrorToast.show("Could not play on the MIDI port", failure);
                            }
                        }));
            });
            add(send);
        }
    }

}
