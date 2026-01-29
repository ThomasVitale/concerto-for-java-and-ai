package com.thomasvitale.mousike.directornote.ui;

import java.util.ArrayList;
import java.util.List;

import com.thomasvitale.mousike.directornote.domain.DirectorNote;
import com.thomasvitale.mousike.directornote.domain.DirectorNoteService;
import com.thomasvitale.mousike.directornote.domain.DirectorNoteService.ExtractedDirectorNote;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

import org.vaadin.lineawesome.LineAwesomeIconUrl;

@Menu(icon = LineAwesomeIconUrl.FILM_SOLID, order = 2)
@PageTitle("Director Notes")
@Route(value = "/director-notes")
public class DirectorNotesView extends Composite<VerticalLayout> {

    private final DirectorNoteService directorNoteService;
    private final List<MarkerField> markers = new ArrayList<>();
    private TextField movieTitle;
    private TextArea sceneDescription;
    private FormLayout formLayout;
    private Button saveButton;
    private Button cancelButton;

    public DirectorNotesView(DirectorNoteService directorNoteService) {
        this.directorNoteService = directorNoteService;
        VerticalLayout pageLayout = new VerticalLayout();
        pageLayout.setWidth("100%");
        pageLayout.setMaxWidth("800px");
        pageLayout.setHeight("min-content");

        pageLayout.add(buildTitle());
        pageLayout.add(buildForm());
        pageLayout.add(buildButtons());

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setJustifyContentMode(JustifyContentMode.START);
        getContent().setAlignItems(Alignment.CENTER);
        getContent().add(pageLayout);

        cancelButton.addClickListener(e -> {
            Notification.show("Not implemented");
        });
        saveButton.addClickListener(e -> {
            var saved = directorNoteService.save(buildDirectorNote());
            Notification.show("Director Note created with ID %s".formatted(saved.id()));
        });
    }

    private DirectorNote buildDirectorNote() {
        var markerList = markers.stream().map(MarkerField::generateModelValue).toList();
        return new DirectorNote(null, movieTitle.getValue(), sceneDescription.getValue(), markerList);
    }

    private Component buildTitle() {
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        titleLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H3 h3 = new H3();
        h3.setText("Add Director Note");

        Button uploadButton = new Button("Upload Audio", VaadinIcon.UPLOAD.create());
        uploadButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_CONTRAST);
        uploadButton.addClickListener(e -> openUploadDialog());

        titleLayout.add(h3, uploadButton);
        return titleLayout;
    }

    private void openUploadDialog() {
        UploadAudioDialog dialog = new UploadAudioDialog(directorNoteService, this::populateForm);
        dialog.open();
    }

    private void populateForm(ExtractedDirectorNote extractedDirectorNote) {
        // Clear existing markers
        markers.forEach(marker -> formLayout.remove(marker));
        markers.clear();

        // Populate text fields
        movieTitle.setValue(extractedDirectorNote.movie());
        sceneDescription.setValue(extractedDirectorNote.sceneDescription());

        // Add markers
        extractedDirectorNote.markers().forEach(marker -> {
            MarkerField markerField = new MarkerField();
            markerField.setPresentationValue(marker);
            formLayout.add(markerField, 2);
            markers.add(markerField);
        });
    }

    private FormLayout buildForm() {
        formLayout = new FormLayout();
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

}
