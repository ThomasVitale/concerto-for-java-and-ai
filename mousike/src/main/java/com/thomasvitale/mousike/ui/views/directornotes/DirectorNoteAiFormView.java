package com.thomasvitale.mousike.ui.views.directornotes;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thomasvitale.mousike.domain.directornote.DirectorNote;
import com.thomasvitale.mousike.domain.directornote.DirectorNoteService;
import com.thomasvitale.mousike.ui.layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

import org.springframework.core.io.InputStreamResource;

@PageTitle("Director Note Form")
@Route(value = "director-note-ai-form", layout = MainLayout.class)
public class DirectorNoteAiFormView extends Composite<VerticalLayout> {

    private final DirectorNoteService directorNoteService;
    private final ObjectMapper objectMapper;
    private TextArea unstructuredNote;
    private Button saveButton;
    private Button cancelButton;
    private Upload upload;

    public DirectorNoteAiFormView(DirectorNoteService directorNoteService, ObjectMapper objectMapper) {
        this.directorNoteService = directorNoteService;
        this.objectMapper = objectMapper;

        VerticalLayout pageLayout = new VerticalLayout();
        pageLayout.setWidth("100%");
        pageLayout.setMaxWidth("800px");
        pageLayout.setHeight("min-content");

        pageLayout.add(buildTitle());
        pageLayout.add(buildForm());
        pageLayout.add(buildButtons());
        pageLayout.add(buildUpload());

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setJustifyContentMode(JustifyContentMode.START);
        getContent().setAlignItems(Alignment.CENTER);
        getContent().add(pageLayout);

        cancelButton.addClickListener(e -> {
            Notification.show("Not implemented");
        });
        saveButton.addClickListener(e -> {
            var saved = directorNoteService.function(buildDirectorNote());
            try {
                pageLayout.add(buildStructuredNote(saved));
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
            Notification.show("Director Note created with ID %s".formatted(saved.id()));
        });
    }

    private Component buildUpload() {
        HorizontalLayout uploadLayout = new HorizontalLayout();
        uploadLayout.addClassName(Gap.MEDIUM);
        uploadLayout.setWidth("100%");
        uploadLayout.getStyle().set("flex-grow", "1");

        MemoryBuffer buffer = new MemoryBuffer();
        upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            InputStream inputStream = buffer.getInputStream();
            try {
                var transcription = directorNoteService.transcribe(new InputStreamResource(inputStream));
                unstructuredNote.setValue(transcription);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        uploadLayout.add(upload);

        return uploadLayout;
    }

    private String buildDirectorNote() {
        return unstructuredNote.getValue();
    }

    private Component buildTitle() {
        H3 h3 = new H3();
        h3.setText("Director Note");
        h3.setWidth("100%");
        return h3;
    }

    private FormLayout buildForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("100%");

        unstructuredNote = new TextArea();
        unstructuredNote.setLabel("Text");

        formLayout.add(unstructuredNote, 2);

        return formLayout;
    }

    private HorizontalLayout buildButtons() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName(Gap.MEDIUM);
        buttonLayout.setWidth("100%");
        buttonLayout.getStyle().set("flex-grow", "1");

        saveButton = new Button();
        saveButton.setText("Save");
        saveButton.setWidth("min-content");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancelButton = new Button();
        cancelButton.setText("Cancel");
        cancelButton.setWidth("min-content");

        buttonLayout.add(saveButton);
        buttonLayout.add(cancelButton);

        return buttonLayout;
    }

    private Component buildStructuredNote(DirectorNote directorNote) throws JsonProcessingException {
        VerticalLayout noteLayout = new VerticalLayout();
        noteLayout.setWidth("100%");

        TextArea structuredNote = new TextArea();
        structuredNote.setWidth("100%");
        structuredNote.setValue(
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(directorNote)
        );

        noteLayout.add(structuredNote);
        return noteLayout;
    }

}
