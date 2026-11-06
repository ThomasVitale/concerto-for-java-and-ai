package com.thomasvitale.mousike.scenes.ui.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.thomasvitale.mousike.scenes.Medium;
import com.thomasvitale.mousike.scenes.Scene;
import com.thomasvitale.mousike.scenes.SceneDraft;
import com.thomasvitale.mousike.scenes.SceneMarker;
import com.thomasvitale.mousike.scenes.SceneService;
import com.thomasvitale.mousike.scenes.Transcript;
import com.thomasvitale.mousike.ui.ErrorToast;
import com.thomasvitale.mousike.ui.ModelBadge;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.streams.UploadHandler;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.task.AsyncTaskExecutor;

/**
 * A new scene. In extended mode, three beats: the recording (or typed notes), the transcript, the structured scene the
 * composer checks. In plain mode, as before speech-to-text and structured output: a form filled in by hand.
 */
class NewSceneDialog extends Dialog {

    private static final Logger log = LoggerFactory.getLogger(NewSceneDialog.class);

    private final SceneService sceneService;
    private final AsyncTaskExecutor executor;
    private final Consumer<Scene> onSaved;

    private final TextArea transcript = new TextArea("What the director said");
    private final Button transcribe = new Button("Transcribe recording", VaadinIcon.MICROPHONE.create());
    private final Button extract = new Button("Structure into a scene");
    private final Div provenance = new Div();
    private @Nullable ModelBadge transcribedBy;
    private @Nullable ModelBadge structuredBy;

    private final Div sceneForm = new Div();
    private final TextField production = new TextField("Production");
    private final TextField title = new TextField("Scene");
    private final TextArea description = new TextArea("What happens");
    private final Select<Medium> medium = new Select<>();
    private final TextField genre = new TextField("Genre");
    private final Div markers = new Div();
    private final List<MarkerRow> markerRows = new ArrayList<>();
    private final Button save = new Button("Save scene");

    private byte @Nullable [] audio;
    private @Nullable String audioName;

    NewSceneDialog(SceneService sceneService, AsyncTaskExecutor executor, boolean extended, Consumer<Scene> onSaved) {
        this.sceneService = sceneService;
        this.executor = executor;
        this.onSaved = onSaved;

        setHeaderTitle("New scene");
        setWidth("min(44rem, 94vw)");
        setCloseOnOutsideClick(false);
        addClassName("new-scene-dialog");

        Upload upload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            audio = bytes;
            audioName = metadata.fileName();
            getUI().ifPresent(ui -> ui.access(() -> transcribe.setEnabled(true)));
        }));
        upload.setMaxFiles(1);
        upload.setAcceptedFileExtensions(".wav", ".mp3", ".m4a", ".ogg", ".webm");
        upload.setWidthFull();
        upload.addFileRemovedListener(e -> {
            audio = null;
            transcribe.setEnabled(false);
        });
        upload.setMaxFileSize(25 * 1024 * 1024);

        transcribe.setEnabled(false);
        transcribe.addClickListener(e -> runTranscription());

        transcript.setWidthFull();
        transcript.setMinHeight("8rem");
        transcript.setPlaceholder("Upload the recording and transcribe it, or type the director's notes here.");
        transcript.setValueChangeMode(ValueChangeMode.EAGER);
        transcript.addValueChangeListener(e -> extract.setEnabled(!e.getValue().isBlank()));

        extract.setEnabled(false);
        extract.addThemeVariants(ButtonVariant.PRIMARY);
        extract.addClickListener(e -> runExtraction());

        provenance.addClassName("provenance-row");

        medium.setLabel("Medium");
        medium.setItems(Medium.values());
        medium.setItemLabelGenerator(m -> m == null ? "" : m.label());
        medium.setValue(Medium.FILM);

        production.setWidthFull();
        production.setPlaceholder("The film, series, game or show");
        title.setWidthFull();
        description.setWidthFull();
        description.setMinHeight("5rem");
        genre.setWidth("12rem");
        HorizontalLayout meta = new HorizontalLayout(medium, genre);
        meta.setPadding(false);

        markers.addClassName("marker-list");
        Span markersLabel = new Span("Time markers");
        markersLabel.addClassName("field-label");
        Button addMarker = new Button("Add marker", VaadinIcon.PLUS.create(), e -> addMarkerRow(new SceneMarker("", "")));
        addMarker.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);

        sceneForm.addClassName("scene-form");
        sceneForm.add(production, title, description, meta, markersLabel, markers, addMarker);

        Div body = new Div();
        body.addClassName("new-scene-dialog-body");
        if (extended) {
            // From the director's voice: the form appears once the transcript has been structured
            sceneForm.setVisible(false);
            body.add(upload, new Div(transcribe), transcript, new Div(extract), provenance, sceneForm);
        }
        else {
            // By hand: the form straight away, with one marker row to start from
            addMarkerRow(new SceneMarker("", ""));
            body.add(sceneForm);
        }
        add(body);

        Button cancel = new Button("Cancel", e -> close());
        cancel.addThemeVariants(ButtonVariant.TERTIARY);
        save.addThemeVariants(ButtonVariant.PRIMARY);
        save.setEnabled(!extended);
        save.addClickListener(e -> saveScene());
        getFooter().add(cancel, save);
        if (!extended) {
            production.focus();
        }
    }

    private void runTranscription() {
        byte[] bytes = audio;
        String name = audioName;
        if (bytes == null || name == null) {
            return;
        }
        UI ui = UI.getCurrent();
        transcribe.setEnabled(false);
        transcribe.setText("Listening to the recording…");
        CompletableFuture.supplyAsync(() -> sceneService.transcribe(bytes, name), executor)
                .whenComplete((result, failure) -> ui.access(() -> {
                    transcribe.setText("Transcribe recording");
                    transcribe.setEnabled(true);
                    if (failure == null) {
                        Transcript t = result;
                        transcript.setValue(t.text());
                        transcribedBy = new ModelBadge(t.model(), t.duration());
                        showProvenance();
                    }
                    else {
                        log.error("Transcription failed", failure);
                        ErrorToast.show("Transcription failed", failure);
                    }
                }));
    }

    private void runExtraction() {
        String text = transcript.getValue();
        if (text.isBlank()) {
            return;
        }
        UI ui = UI.getCurrent();
        extract.setEnabled(false);
        extract.setText("Reading the notes…");
        Medium chosen = medium.getValue();
        CompletableFuture.supplyAsync(() -> sceneService.draft(text, chosen), executor)
                .whenComplete((draft, failure) -> ui.access(() -> {
                    extract.setText("Structure into a scene");
                    extract.setEnabled(true);
                    if (failure == null) {
                        showDraft(draft);
                    }
                    else {
                        log.error("Extraction failed", failure);
                        ErrorToast.show("Could not structure the transcript", failure);
                    }
                }));
    }

    private void showDraft(SceneDraft draft) {
        production.setValue(draft.production());
        title.setValue(draft.title());
        description.setValue(draft.description());
        genre.setValue(draft.genre() == null ? "" : draft.genre());
        markers.removeAll();
        markerRows.clear();
        draft.markers().forEach(this::addMarkerRow);
        if (draft.markers().isEmpty()) {
            addMarkerRow(new SceneMarker("", ""));
        }
        structuredBy = new ModelBadge(draft.model(), draft.duration());
        showProvenance();
        sceneForm.setVisible(true);
        save.setEnabled(true);
        title.focus();
    }

    /** The latest run of each step: running one again replaces its badge. */
    private void showProvenance() {
        provenance.removeAll();
        if (transcribedBy != null) {
            provenance.add(transcribedBy);
        }
        if (structuredBy != null) {
            provenance.add(structuredBy);
        }
    }

    private void addMarkerRow(SceneMarker marker) {
        MarkerRow row = new MarkerRow(marker, removed -> {
            markers.remove(removed);
            markerRows.remove(removed);
        });
        markerRows.add(row);
        markers.add(row);
    }

    private void saveScene() {
        if (title.getValue().isBlank()) {
            title.setInvalid(true);
            title.setErrorMessage("Give the scene a name");
            return;
        }
        List<MarkerRow> filled = markerRows.stream().filter(r -> !r.isBlank()).toList();
        // Every row answers, so each bad time shows at once
        if (filled.stream().map(MarkerRow::validate).toList().contains(false)) {
            return;
        }
        List<SceneMarker> values = filled.stream().map(MarkerRow::value).toList();
        Scene scene = Scene.create(production.getValue(), title.getValue(), description.getValue(), medium.getValue(), genre.getValue(),
                transcript.getValue().isBlank() ? null : transcript.getValue(), values);
        Scene saved = sceneService.save(scene);
        onSaved.accept(saved);
        close();
    }

}
