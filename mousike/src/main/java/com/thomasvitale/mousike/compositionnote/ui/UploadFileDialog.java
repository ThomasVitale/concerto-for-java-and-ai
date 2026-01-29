package com.thomasvitale.mousike.compositionnote.ui;

import java.util.Map;
import java.util.function.Consumer;

import com.thomasvitale.mousike.compositionnote.domain.CompositionNoteService;
import com.thomasvitale.mousike.compositionnote.domain.CompositionNoteService.ExtractedCompositionNote;
import com.thomasvitale.mousike.document.DocumentProcessingService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

class UploadFileDialog extends Dialog {

    private final DocumentProcessingService documentProcessingService;
    private final CompositionNoteService compositionNoteService;
    private final UploadFileForm form;
    private final Consumer<ExtractedCompositionNote> onExtractCallback;

    UploadFileDialog(DocumentProcessingService documentProcessingService,
                     CompositionNoteService compositionNoteService,
                     Consumer<ExtractedCompositionNote> onExtractCallback) {
        this.documentProcessingService = documentProcessingService;
        this.compositionNoteService = compositionNoteService;
        this.onExtractCallback = onExtractCallback;

        // Create components
        form = new UploadFileForm();

        var extractButton = new Button("Extract", e -> extractFromFile());
        extractButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        extractButton.addClickShortcut(Key.ENTER);

        var closeButton = new Button("Close", e -> close());
        closeButton.addClickShortcut(Key.ESCAPE);

        // Layout dialog
        setHeaderTitle("Upload File");
        add(form);
        getFooter().add(closeButton, extractButton);
        setWidth("600px");
    }

    private void extractFromFile() {
        if (!form.hasUploadedFile()) {
            Notification.show("Please upload a file first");
            return;
        }

        try {
            // Get the uploaded file with its name
            Map.Entry<String, byte[]> fileEntry = form.getUploadedFile();
            String fileName = fileEntry.getKey();
            byte[] fileData = fileEntry.getValue();

            // Create a ByteArrayResource with filename
            Resource resource = new ByteArrayResource(fileData) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };

            // Process the document to extract text content
            String documentContent = documentProcessingService.process(resource);

            // Extract structured data from the content
            ExtractedCompositionNote extractedCompositionNote = compositionNoteService.structure(documentContent);

            // Notify success
            Notification notification = Notification.show(
                "File processed and data extracted successfully",
                3000,
                Notification.Position.BOTTOM_START
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Clean up and close
            form.clearUploadedFiles();

            // Call the callback to populate the form
            if (onExtractCallback != null) {
                onExtractCallback.accept(extractedCompositionNote);
            }

            close();

        } catch (Exception e) {
            Notification notification = Notification.show(
                "Error processing file: " + e.getMessage(),
                5000,
                Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
