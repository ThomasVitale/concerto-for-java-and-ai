package com.thomasvitale.mousike.scenes.ui.internal;

import java.util.function.Consumer;

import com.thomasvitale.mousike.scenes.SceneMarker;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;

/**
 * An editable time marker: when, and what the director asked for.
 */
class MarkerRow extends Div {

    private static final String TIME_PATTERN = "^\\d{1,2}:\\d{2}(:\\d{2})?$";

    private final TextField time = new TextField();
    private final TextField comment = new TextField();

    MarkerRow(SceneMarker marker, Consumer<MarkerRow> onRemove) {
        addClassName("marker-row");
        time.setValue(marker.timeMarker());
        time.setPlaceholder("mm:ss");
        time.setPattern(TIME_PATTERN);
        time.setErrorMessage("Use mm:ss");
        time.setAllowedCharPattern("[0-9:]");
        time.addClassName("marker-time");
        time.setWidth("6rem");
        comment.setValue(marker.comment());
        comment.setPlaceholder("What should the music do here?");
        comment.addClassName("marker-comment");
        Button remove = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> onRemove.accept(this));
        remove.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
        remove.setAriaLabel("Remove marker");
        remove.setTooltipText("Remove");
        add(time, comment, remove);
    }

    /** The field's pattern only marks the field; saving asks here, and a bad time shows on the row. */
    boolean validate() {
        String value = time.getValue().strip();
        boolean valid = value.isEmpty() || value.matches(TIME_PATTERN);
        time.setInvalid(!valid);
        return valid;
    }

    SceneMarker value() {
        return new SceneMarker(time.getValue().strip(), comment.getValue().strip());
    }

    boolean isBlank() {
        return time.getValue().isBlank() && comment.getValue().isBlank();
    }

}
