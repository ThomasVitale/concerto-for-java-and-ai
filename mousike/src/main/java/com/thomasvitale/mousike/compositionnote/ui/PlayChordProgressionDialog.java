package com.thomasvitale.mousike.compositionnote.ui;

import com.thomasvitale.mousike.midi.MidiService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;

class PlayChordProgressionDialog extends Dialog {

    private final MidiService midiService;
    private final PlayChordProgressionForm form;

    PlayChordProgressionDialog(MidiService midiService) {
        this.midiService = midiService;

        // Create components
        form = new PlayChordProgressionForm();

        var playButton = new Button("Play", e -> playChordProgression());
        playButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        playButton.addClickShortcut(Key.ENTER);

        var closeButton = new Button("Close", e -> close());
        closeButton.addClickShortcut(Key.ESCAPE);

        // Layout dialog
        setHeaderTitle("Play a Chord Progression");
        add(form);
        getFooter().add(closeButton, playButton);
        setWidth("600px");
    }

    private void playChordProgression() {
        if (!form.hasChordProgression()) {
            Notification.show("Please enter a chord progression");
            return;
        }

        if (!form.hasKey()) {
            Notification.show("Please enter a key");
            return;
        }

        try {
            String chordProgression = form.getChordProgression();
            String key = form.getKey();
            String question = "Play the chord progression " + chordProgression + " in the key of " + key;
            String response = midiService.answer(question);

            if (response != null && !response.isEmpty()) {
                Notification.show("Playing: " + response, 5000, Notification.Position.BOTTOM_START);
            } else {
                Notification.show("Chord progression played successfully", 3000, Notification.Position.BOTTOM_START);
            }
        } catch (Exception e) {
            Notification.show("Error playing chord progression: " + e.getMessage());
        }
    }

}
