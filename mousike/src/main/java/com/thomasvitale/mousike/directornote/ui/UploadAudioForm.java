package com.thomasvitale.mousike.directornote.ui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;

class UploadAudioForm extends Composite<VerticalLayout> {

    private final Map<String, byte[]> uploadedFiles = new ConcurrentHashMap<>();
    private final Upload upload;

    UploadAudioForm() {
        InMemoryUploadHandler uploadHandler = UploadHandler.inMemory(
            (metadata, data) -> {
                // Store uploaded file data in memory temporarily
                uploadedFiles.put(metadata.fileName(), data);
            });

        upload = new Upload(uploadHandler);
        upload.setWidthFull();
        upload.setMaxFiles(1);
        upload.setAcceptedFileTypes("audio/*", ".mp3", ".wav", ".m4a", ".ogg");

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
