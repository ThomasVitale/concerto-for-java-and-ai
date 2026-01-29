package com.thomasvitale.mousike.compositionnote.ui;

import com.thomasvitale.mousike.compositionnote.domain.CompositionNote;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import org.jspecify.annotations.Nullable;

class CompositionNoteDrawer extends Composite<VerticalLayout> {

    @FunctionalInterface
    interface SaveCallback {
        CompositionNote save(CompositionNote productDetails);
    }

    @FunctionalInterface
    interface ErrorCallback {
        void handleException(RuntimeException e);
    }

    private final SaveCallback saveCallback;
    private final ErrorCallback errorCallback;
    private final CompositionNoteForm form;

    CompositionNoteDrawer(SaveCallback saveCallback, ErrorCallback errorCallback) {
        this.saveCallback = saveCallback;
        this.errorCallback = errorCallback;

        var header = new H2("Composition Note");
        form = new CompositionNoteForm();

        var saveButton = new Button("Save", e -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var layout = getContent();
        layout.add(header);
        layout.add(new Scroller(form));
        layout.add(saveButton);
        layout.setWidth("300px");
        addClassName(LumoUtility.BoxShadow.MEDIUM);
        setVisible(false);
    }

    public void setCompositionNote(@Nullable CompositionNote compositionNote) {
        form.setFormDataObject(compositionNote);
        setVisible(compositionNote != null);
    }

    private void save() {
        form.getFormDataObject().ifPresent(compositionNote -> {
            try {
                var saved = saveCallback.save(compositionNote);
                form.setFormDataObject(saved);
            } catch (RuntimeException e) {
                errorCallback.handleException(e);
            }
        });
    }
}
