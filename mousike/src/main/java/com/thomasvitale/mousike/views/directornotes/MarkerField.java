package com.thomasvitale.mousike.views.directornotes;

import com.thomasvitale.mousike.domain.directornote.Marker;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

public class MarkerField extends CustomField<Marker> {
    private final TextField time;
    private final TextArea note;

    public MarkerField() {
        time = new TextField();
        time.setLabel("Time");
        time.setWidth("50%");

        note = new TextArea();
        note.setLabel("Note");
        note.setWidth("50%");

        HorizontalLayout layout = new HorizontalLayout(time, note);
        layout.setFlexGrow(1.0, time, note);

        add(layout);
    }

    @Override
    protected Marker generateModelValue() {
        return new Marker(time.getValue(), note.getValue());
    }

    @Override
    protected void setPresentationValue(Marker marker) {
        time.setValue(marker.time());
        note.setValue(marker.note());
    }

}
