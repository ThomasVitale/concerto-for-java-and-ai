package com.thomasvitale.mousike.directornote.ui;

import java.util.Map;
import java.util.function.Consumer;

import com.thomasvitale.mousike.directornote.domain.DirectorNoteService;
import com.thomasvitale.mousike.directornote.domain.DirectorNoteService.ExtractedDirectorNote;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

class UploadAudioDialog extends Dialog {

    private final DirectorNoteService directorNoteService;
    private final UploadAudioForm form;
    private final Consumer<ExtractedDirectorNote> onExtractCallback;

    UploadAudioDialog(DirectorNoteService directorNoteService, Consumer<ExtractedDirectorNote> onExtractCallback) {
        this.directorNoteService = directorNoteService;
        this.onExtractCallback = onExtractCallback;

        // Create components
        form = new UploadAudioForm();

        var extractButton = new Button("Extract", e -> extractFromAudio());
        extractButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        extractButton.addClickShortcut(Key.ENTER);

        var closeButton = new Button("Close", e -> close());
        closeButton.addClickShortcut(Key.ESCAPE);

        // Layout dialog
        setHeaderTitle("Upload Audio File");
        add(form);
        getFooter().add(closeButton, extractButton);
        setWidth("600px");
    }

    private void extractFromAudio() {
        if (!form.hasUploadedFile()) {
            Notification.show("Please upload an audio file first");
            return;
        }

        try {
            // Get the uploaded file with its name
            Map.Entry<String, byte[]> fileEntry = form.getUploadedFile();
            String fileName = fileEntry.getKey();
            byte[] fileData = fileEntry.getValue();

            // Create a ByteArrayResource with filename for Spring AI
            Resource resource = new ByteArrayResource(fileData) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };

            // Transcribe and extract structured data
            String transcription = directorNoteService.transcribe(resource);
            ExtractedDirectorNote extractedDirectorNote = directorNoteService.structure(transcription);

            // Notify success and callback
            Notification notification = Notification.show(
                "Audio transcribed and data extracted successfully",
                3000,
                Notification.Position.BOTTOM_START
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Clean up and close
            form.clearUploadedFiles();
            
            // Call the callback to populate the form
            if (onExtractCallback != null) {
                onExtractCallback.accept(extractedDirectorNote);
            }
            
            close();

        } catch (Exception e) {
            Notification notification = Notification.show(
                "Error processing audio file: " + e.getMessage(),
                5000,
                Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
