package com.thomasvitale.mousike.ui.views.compositionnotes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.thomasvitale.mousike.domain.compositionnote.CompositionNote;
import com.thomasvitale.mousike.domain.compositionnote.CompositionNoteService;
import com.thomasvitale.mousike.ui.layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import org.springframework.core.env.Environment;

@PageTitle("Composition Notes")
@Route(value = "/legacy/:compositionNoteId?/:action?(edit)", layout = MainLayout.class)
@Uses(Icon.class)
public class CompositionNoteView extends Div implements BeforeEnterObserver {

    private final String COMPOSITION_NOTE_ID = "compositionNoteId";
    private final String COMPOSITION_NOTE_EDIT_ROUTE_TEMPLATE = "/%s/edit";

    private final Grid<CompositionNote> grid = new Grid<>(CompositionNote.class, false);

    private Select<CompositionNote.Type> type;
    private TextArea content;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<CompositionNote> binder;

    private CompositionNote compositionNote;

    private final CompositionNoteService compositionNoteService;

    public CompositionNoteView(CompositionNoteService compositionNoteService, Environment environment) {
        this.compositionNoteService = compositionNoteService;
        addClassNames("master-detail-view");

        VerticalLayout pageLayout = new VerticalLayout();
        pageLayout.setHeightFull();

        pageLayout.add(buildKeywordSearch());

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        // Configure Grid
        grid.addColumn("type").setAutoWidth(true);
        grid.addColumn("content").setAutoWidth(true);

        grid.setItems(compositionNoteService.findAll());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(COMPOSITION_NOTE_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(CompositionNoteView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(CompositionNote.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.compositionNote == null) {
                    this.compositionNote = new CompositionNote();
                }
                binder.writeBean(this.compositionNote);

                if (compositionNote.getType() == null) {
                    try {
                        compositionNoteService.classify(compositionNote);
                    } catch (Exception ex) {
                        throw new ValidationException(List.of(), List.of());
                    }
                } else {
                    compositionNoteService.save(this.compositionNote);
                }

                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(CompositionNoteView.class);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });

        pageLayout.add(splitLayout);

        add(pageLayout);
    }

    private Component buildKeywordSearch() {
        FormLayout formLayout = new FormLayout();

        TextField searchField = new TextField("Search");
        searchField.setPlaceholder("Enter search term");

        Button searchButton = new Button("Keyword Search");

        formLayout.add(searchField, searchButton);

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1), // 1 column for narrow screens
                new FormLayout.ResponsiveStep("600px", 2) // 2 columns for wider screens
        );

        searchButton.addClickListener(event -> {
            var query = searchField.getValue();
            grid.setItems(compositionNoteService.keywordSearch(query));
        });

        return formLayout;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> compositionNoteId = event.getRouteParameters().get(COMPOSITION_NOTE_ID).map(UUID::fromString);
        if (compositionNoteId.isPresent()) {
            Optional<CompositionNote> compositionNoteFromBackend = compositionNoteService.findById(compositionNoteId.get());
            if (compositionNoteFromBackend.isPresent()) {
                populateForm(compositionNoteFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested compositionNote was not found, ID = %s", compositionNoteId.get()), 3000,
                        Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(CompositionNoteView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();

        type = new Select<>();
        type.setLabel("Type");
        type.setItems(CompositionNote.Type.values());

        content = new TextArea("Content");

        formLayout.add(type, content);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
        grid.setItems(compositionNoteService.findAll());
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(CompositionNote value) {
        this.compositionNote = value;
        binder.readBean(this.compositionNote);

    }
}
