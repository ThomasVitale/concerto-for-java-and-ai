package com.thomasvitale.mousike.compositionnote.ui;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;

class PlayChordProgressionForm extends Composite<FormLayout> {

    private final TextField chordProgressionField;
    private final TextField keyField;

    PlayChordProgressionForm() {
        // Create components
        chordProgressionField = new TextField("Chord Progression");
        chordProgressionField.setPlaceholder("i VI III VII");
        chordProgressionField.setWidthFull();

        keyField = new TextField("Key");
        keyField.setPlaceholder("Dm");
        keyField.setWidthFull();

        // Layout form
        var layout = getContent();
        layout.add(chordProgressionField);
        layout.add(keyField);
    }

    public String getChordProgression() {
        return chordProgressionField.getValue();
    }

    public String getKey() {
        return keyField.getValue();
    }

    public boolean hasChordProgression() {
        return chordProgressionField.getValue() != null && !chordProgressionField.getValue().trim().isEmpty();
    }

    public boolean hasKey() {
        return keyField.getValue() != null && !keyField.getValue().trim().isEmpty();
    }
}
