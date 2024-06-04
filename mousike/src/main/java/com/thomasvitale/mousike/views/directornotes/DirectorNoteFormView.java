package com.thomasvitale.mousike.views.directornotes;

import java.util.ArrayList;
import java.util.List;

import com.thomasvitale.mousike.domain.directornote.DirectorNote;
import com.thomasvitale.mousike.domain.directornote.DirectorNoteService;
import com.thomasvitale.mousike.views.MainLayout;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

@PageTitle("Director Note Form")
@Route(value = "director-note-form", layout = MainLayout.class)
public class DirectorNoteFormView extends Composite<VerticalLayout> {

    private TextField movieTitle;
    private TextArea sceneDescription;
    private List<MarkerField> markers = new ArrayList<>();
    private Button saveButton;
    private Button cancelButton;

    public DirectorNoteFormView(DirectorNoteService directorNoteService) {
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
        H3 h3 = new H3();
        h3.setText("Director Note");
        h3.setWidth("100%");
        return h3;
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
