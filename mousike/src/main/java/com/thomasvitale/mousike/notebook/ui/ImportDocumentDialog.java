package com.thomasvitale.mousike.notebook.ui;

import com.thomasvitale.mousike.notebook.NoteImporter;
import com.thomasvitale.mousike.ui.ErrorToast;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

class ImportDocumentDialog extends Dialog {

    private static final Logger log = LoggerFactory.getLogger(ImportDocumentDialog.class);

    private final NoteImporter importer;
    private final AsyncTaskExecutor executor;
    private final BiConsumer<String, String> onRead;

    private final Span hint = new Span("A photo of a notebook page, a PDF, a Word file or plain text.");
    private final ProgressBar progress = new ProgressBar();
    private final Button cancel = new Button("Cancel", e -> close());
    private final Button readButton = new Button("Read document");

    private @Nullable String fileName;
    private byte @Nullable [] data;

    /** {@code onRead} receives the file name and the text Docling read. */
    ImportDocumentDialog(NoteImporter importer, AsyncTaskExecutor executor, BiConsumer<String, String> onRead) {
        this.importer = importer;
        this.executor = executor;
        this.onRead = onRead;

        setHeaderTitle("Import a document");
        setWidth("min(36rem, 92vw)");
        setCloseOnOutsideClick(false);

        Upload upload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            fileName = metadata.fileName();
            data = bytes;
            getUI().ifPresent(ui -> ui.access(() -> readButton.setEnabled(true)));
        }));
        upload.setMaxFiles(1);
        upload.setMaxFileSize(25 * 1024 * 1024);
        upload.setAcceptedFileExtensions(".pdf", ".jpg", ".jpeg", ".png", ".md", ".txt", ".html", ".docx");
        upload.setWidthFull();
        upload.addFileRemovedListener(e -> {
            data = null;
            readButton.setEnabled(false);
        });

        hint.addClassName("note-editor-hint");
        progress.setIndeterminate(true);
        progress.setVisible(false);

        VerticalLayout body = new VerticalLayout(upload, progress, hint);
        body.setPadding(false);
        add(body);

        cancel.addThemeVariants(ButtonVariant.TERTIARY);
        readButton.addThemeVariants(ButtonVariant.PRIMARY);
        readButton.setEnabled(false);
        readButton.addClickListener(e -> read());
        getFooter().add(cancel, readButton);
    }

    private void read() {
        byte[] bytes = data;
        String name = fileName;
        if (bytes == null || name == null) {
            return;
        }
        UI ui = UI.getCurrent();
        readButton.setEnabled(false);
        cancel.setEnabled(false);
        setCloseOnEsc(false);
        readButton.setText("Reading…");
        progress.setVisible(true);

        CompletableFuture.supplyAsync(() -> importer.extractText(name, bytes), executor)
                .whenComplete((text, failure) -> ui.access(() -> {
                    if (failure == null && !text.isBlank()) {
                        close();
                        onRead.accept(name, text);
                        return;
                    }
                    if (failure != null) {
                        log.error("Could not read document", failure);
                        ErrorToast.show("Couldn't read that file", failure);
                    }
                    else {
                        ErrorToast.show("Couldn't find any text in %s.".formatted(name));
                    }
                    progress.setVisible(false);
                    cancel.setEnabled(true);
                    setCloseOnEsc(true);
                    readButton.setEnabled(true);
                    readButton.setText("Read document");
                }));
    }

}
