package com.thomasvitale.mousike.ui.views.directornotes;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.thomasvitale.mousike.domain.directornote.DirectorNote;
import com.thomasvitale.mousike.domain.directornote.DirectorNoteService;
import com.thomasvitale.mousike.domain.directornote.Marker;
import com.thomasvitale.mousike.domain.directornote.DirectorNoteService.ExtractedDirectorNote;
import com.thomasvitale.mousike.ui.layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

@PageTitle("Director Note Form")
@Route(value = "director-note-ai-form", layout = MainLayout.class)
public class DirectorNoteAiFormView extends VerticalLayout {

    private final DirectorNoteService directorNoteService;
    private ExtractedDirectorNote extractedDirectorNote;

    private FormLayout form;
    private TextField movieTitle;
    private TextArea sceneDescription;
    private List<MarkerField> markers = new ArrayList<>();

    public DirectorNoteAiFormView(DirectorNoteService directorNoteService) {
        this.directorNoteService = directorNoteService;

        add(buildTitle());

        add(buildUpload());

        add(new Hr());

        form = buildForm();
        add(form);
        add(buildButton());
    }

    private H3 buildTitle() {
        var title = new H3("Director Note");
        //title.addClassNames(LumoUtility.FontSize.XLARGE);
        return title;
    }

    private HorizontalLayout buildUpload() {
        HorizontalLayout uploadLayout = new HorizontalLayout();
        uploadLayout.setWidthFull();
        uploadLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setWidthFull();

        Button extractButton = new Button("Extract");
        extractButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        extractButton.setDisableOnClick(true);
        extractButton.addClickListener(_ -> {
            List<Resource> resources = new ArrayList<>();
            buffer.getFiles().forEach(file -> {
                try {
                    InputStream inputStream = buffer.getInputStream(file);
                    resources.add(new InputStreamResource(inputStream));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            String transcription;
            try {
                transcription = directorNoteService.transcribe(resources.getFirst());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            extractedDirectorNote = directorNoteService.structure(transcription);
            System.out.println(extractedDirectorNote);
            populateForm(extractedDirectorNote);
            extractButton.setEnabled(true);
        });

        uploadLayout.add(upload);
        uploadLayout.add(extractButton);

        return uploadLayout;
    }

    private FormLayout buildForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("100%");

        movieTitle = new TextField();
        movieTitle.setLabel("Movie Title");

        sceneDescription = new TextArea();
        sceneDescription.setLabel("Scene Description");

        Button addMarkerButton = new Button("Add Marker", event -> {
            MarkerField markerField = new MarkerField();
            formLayout.add(markerField, 2);
            markers.add(markerField);
        });

        formLayout.add(movieTitle, 2);
        formLayout.add(sceneDescription, 2);
        formLayout.add(addMarkerButton, 2);

        return formLayout;
    }

    private HorizontalLayout buildButton() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();

        Button saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(_ -> {
            List<Marker> markerList = new ArrayList<>();
            markers.forEach(m -> markerList.add(m.generateModelValue()));
            
            DirectorNote directorNote = new DirectorNote(
                    null,
                    movieTitle.getValue(),
                    sceneDescription.getValue(),
                    markerList
            );
            DirectorNote createdDirectorNote = directorNoteService.save(directorNote);
            Notification.show("Director Note registered with ID %s".formatted(createdDirectorNote.id()));
        });

        buttonLayout.add(saveButton);

        return buttonLayout;
    }

    private void populateForm(ExtractedDirectorNote extractedDirectorNote) {
        movieTitle.setValue(extractedDirectorNote.movie());
        sceneDescription.setValue(extractedDirectorNote.sceneDescription());
        extractedDirectorNote.markers().forEach(m -> {
            var mf = new MarkerField();
            mf.setPresentationValue(m);
            markers.add(mf);
            form.add(mf, 2);
        });
    }

}
