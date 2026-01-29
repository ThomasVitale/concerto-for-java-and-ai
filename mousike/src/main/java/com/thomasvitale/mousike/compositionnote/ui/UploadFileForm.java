package com.thomasvitale.mousike.compositionnote.ui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;

class UploadFileForm extends Composite<VerticalLayout> {

    private final Map<String, byte[]> uploadedFiles = new ConcurrentHashMap<>();
    private final Upload upload;

    UploadFileForm() {
        InMemoryUploadHandler uploadHandler = UploadHandler.inMemory(
            (metadata, data) -> {
                // Store uploaded file data in memory temporarily
                uploadedFiles.put(metadata.fileName(), data);
            });

        upload = new Upload(uploadHandler);
        upload.setWidthFull();
        upload.setMaxFiles(1);
        upload.setAcceptedFileTypes(
            // Text files
            "text/plain", ".txt",
            "text/markdown", ".md",
            // Documents
            "application/pdf", ".pdf",
            "application/msword", ".doc",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx",
            // Images
            "image/*", ".jpg", ".jpeg", ".png", ".gif"
        );

        // Clean up files when they are removed from the upload component
        upload.addFileRemovedListener(event -> {
            uploadedFiles.remove(event.getFileName());
        });

        var layout = getContent();
        layout.setPadding(false);
        layout.add(upload);
    }

    public boolean hasUploadedFile() {
        return !uploadedFiles.isEmpty();
    }

    public Map.Entry<String, byte[]> getUploadedFile() {
        if (uploadedFiles.isEmpty()) {
            return null;
        }
        return uploadedFiles.entrySet().iterator().next();
    }

    public void clearUploadedFiles() {
        uploadedFiles.clear();
        upload.clearFileList();
    }
}
