package com.thomasvitale.mousike.compositionnote.ui;

import com.thomasvitale.mousike.compositionnote.domain.CompositionNote;
import com.thomasvitale.mousike.compositionnote.domain.CompositionNoteService;
import com.thomasvitale.mousike.compositionnote.domain.CompositionNoteService.ExtractedCompositionNote;
import com.thomasvitale.mousike.document.DocumentProcessingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;

class AddCompositionNoteDialog extends Dialog {

    @FunctionalInterface
    interface SaveCallback {
        void save(CompositionNote compositionNote);
    }

    @FunctionalInterface
    interface ErrorCallback {
        void handleException(RuntimeException e);
    }

    private final SaveCallback saveCallback;
    private final ErrorCallback errorCallback;
    private final DocumentProcessingService documentProcessingService;
    private final CompositionNoteService compositionNoteService;
    private final CompositionNoteForm form;

    AddCompositionNoteDialog(SaveCallback saveCallback,
                            ErrorCallback errorCallback,
                            DocumentProcessingService documentProcessingService,
                            CompositionNoteService compositionNoteService) {
        this.saveCallback = saveCallback;
        this.errorCallback = errorCallback;
        this.documentProcessingService = documentProcessingService;
        this.compositionNoteService = compositionNoteService;

        // Create components
        form = new CompositionNoteForm();
        form.setFormDataObject(new CompositionNote());

        var uploadButton = new Button("Upload File", VaadinIcon.UPLOAD.create());
        uploadButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        uploadButton.addClickListener(e -> openUploadDialog());

        var saveButton = new Button("Save", e -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        var cancelButton = new Button("Cancel", e -> close());

        // Layout dialog
        setHeaderTitle("Add Composition Note");
        getHeader().add(uploadButton);
        add(form);
        getFooter().add(cancelButton, saveButton);
    }

    private void openUploadDialog() {
        UploadFileDialog dialog = new UploadFileDialog(
            documentProcessingService,
            compositionNoteService,
            this::populateForm
        );
        dialog.open();
    }

    private void populateForm(ExtractedCompositionNote extractedNote) {
        CompositionNote compositionNote = new CompositionNote();

        // Set type from extracted data
        try {
            compositionNote.setType(extractedNote.type());
        } catch (IllegalArgumentException e) {
            // If type is not valid, leave it null - will be classified later
            compositionNote.setType(null);
        }

        // Set content
        compositionNote.setContent(extractedNote.content());

        // Update the form with the new data
        form.setFormDataObject(compositionNote);
    }

    private void save() {
        form.getFormDataObject().ifPresent(compositionNote -> {
            try {
                saveCallback.save(compositionNote);
                close();
            } catch (RuntimeException e) {
                errorCallback.handleException(e);
            }
        });
    }

}
