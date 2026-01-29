package com.thomasvitale.mousike.compositionnote.ui;

import java.util.Optional;

import com.thomasvitale.mousike.compositionnote.domain.CompositionNote;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;

import org.jspecify.annotations.Nullable;

class CompositionNoteForm extends Composite<FormLayout>  {

    private final Binder<CompositionNote> binder;

    CompositionNoteForm() {
        // Create components
        var typeField = new Select<CompositionNote.Type>("Type");
        typeField.setItems(CompositionNote.Type.values());
        var contentField = new TextArea("Content");

        // Layout form
        var layout = getContent();
        layout.add(typeField);
        layout.add(contentField);

        // Bind fields
        binder = new Binder<>();
        binder.forField(typeField)
                .bind(CompositionNote::getType,
                        CompositionNote::setType);
        binder.forField(contentField)
                .asRequired("Enter content")
                .bind(CompositionNote::getContent,
                        CompositionNote::setContent);
    }

    public void setFormDataObject(@Nullable CompositionNote compositionNote) {
        binder.setBean(compositionNote);
    }

    public Optional<CompositionNote> getFormDataObject() {
        if (binder.getBean() == null) {
            throw new IllegalStateException("No form data object");
        }
        if (binder.validate().isOk()) {
            return Optional.of(binder.getBean());
        } else {
            return Optional.empty();
        }
    }

}
