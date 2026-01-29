package com.thomasvitale.mousike.compositionnote.ui;

import java.util.Optional;
import java.util.UUID;

import com.thomasvitale.mousike.compositionnote.domain.CompositionNoteItem;
import com.thomasvitale.mousike.compositionnote.domain.CompositionNoteService;
import com.thomasvitale.mousike.document.DocumentProcessingService;
import com.thomasvitale.mousike.midi.MidiService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.streams.DownloadHandler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIcon;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@Menu(icon = LineAwesomeIconUrl.GUITAR_SOLID, order = 1)
@Route("/compositionnote")
@RouteAlias(value = "")
@PageTitle("Composition Notes")
public class CompositionNoteView extends HorizontalLayout implements HasUrlParameter<String> {

    private final CompositionNoteService compositionNoteService;
    private final DocumentProcessingService documentProcessingService;

    private final Grid<CompositionNoteItem> grid;
    private final CompositionNoteDrawer drawer;
    private final RadioButtonGroup<String> searchModeGroup;

    CompositionNoteView(CompositionNoteService compositionNoteService,
                       DocumentProcessingService documentProcessingService,
                       MidiService midiService) {
        this.compositionNoteService = compositionNoteService;
        this.documentProcessingService = documentProcessingService;

        // Create components
        var searchField = new TextField();
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("25%");

        searchModeGroup = new RadioButtonGroup<>();
        searchModeGroup.setItems("Lexical", "Semantic");
        searchModeGroup.setValue("Lexical");

        var questionButton = new Button(LineAwesomeIcon.COMMENTS.create());
        questionButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_CONTRAST);
        questionButton.setTooltipText("Ask a question");
        questionButton.addClickListener(e -> new AddQuestionAnsweringDialog(compositionNoteService).open());

        var mysteryButton = new Button(LineAwesomeIcon.USER_SECRET_SOLID.create());
        mysteryButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_CONTRAST);
        mysteryButton.setTooltipText("Solve a mystery");

        Image image = new Image(DownloadHandler.forClassResource(getClass(), "/images/scooby-doo.jpg"), "Scooby Doo");
        image.setWidth("860px");

        Popover popover = new Popover(image);
        popover.setTarget(mysteryButton);
        popover.setPosition(PopoverPosition.BOTTOM_START);
        popover.addThemeVariants(PopoverVariant.LUMO_NO_PADDING);

        // Add ESC key shortcut to close the popover
        Shortcuts.addShortcutListener(this, popover::close, Key.ESCAPE)
                .bindLifecycleTo(popover);

        var midiButton = new Button(LineAwesomeIcon.MUSIC_SOLID.create());
        midiButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_CONTRAST);
        midiButton.setTooltipText("Play a chord progression");
        midiButton.addClickListener(e -> new PlayChordProgressionDialog(midiService).open());

        grid = new Grid<>();
        grid.addColumn(CompositionNoteItem::type)
                .setHeader("Type")
                .setSortProperty(CompositionNoteItem.SORT_PROPERTY_TYPE);
        grid.addColumn(CompositionNoteItem::content)
                .setHeader("Content");
        grid.setItemsPageable(pageable -> {
            if ("Semantic".equals(searchModeGroup.getValue())) {
                return compositionNoteService.findItemsViaSemanticSearch(searchField.getValue(), pageable);
            } else {
                return compositionNoteService.findItemsViaLexicalSearch(searchField.getValue(), pageable);
            }
        });

        drawer = new CompositionNoteDrawer(productDetails -> {
            var saved = compositionNoteService.save(productDetails);
            grid.getDataProvider().refreshAll();
            return saved;
        }, this::handleException);

        searchField.addValueChangeListener(e -> grid.getDataProvider().refreshAll());
        searchModeGroup.addValueChangeListener(e -> grid.getDataProvider().refreshAll());

        grid.addSelectionListener(e -> e.getFirstSelectedItem()
                .map(CompositionNoteItem::id)
                .ifPresentOrElse(
                        CompositionNoteView::showCompositionNote,
                        CompositionNoteView::showCompositionNotes
                ));

        var addButton = new Button("Add Composition Note", e ->
                new AddCompositionNoteDialog(
                        compositionNote -> {
                            var saved = compositionNoteService.save(compositionNote);
                            grid.getDataProvider().refreshAll();
                            showCompositionNote(saved.getId());
                        },
                        this::handleException,
                        documentProcessingService,
                        compositionNoteService
                ).open()
        );

        // Layout view
        setSizeFull();
        setSpacing(false);

        var toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.addToStart(searchField, searchModeGroup, questionButton, mysteryButton, midiButton);
        toolbar.addToEnd(addButton);
        toolbar.setAlignItems(Alignment.CENTER);

        var listLayout = new VerticalLayout(toolbar, grid);
        listLayout.setSizeFull();
        grid.setSizeFull();

        add(listLayout, drawer);

        setFlexShrink(0, drawer);

        // Add ESC key shortcut to close the drawer
        Shortcuts.addShortcutListener(this,
                CompositionNoteView::showCompositionNotes,
                Key.ESCAPE)
                .bindLifecycleTo(drawer);
    }

    private void handleException(RuntimeException exception) {
        if (exception instanceof OptimisticLockingFailureException) {
            var notification = new Notification(
                    "Another user has edited the same composition note. "
                            + "Please refresh and try again.");
            notification.setPosition(Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            notification.setDuration(3000);
            notification.open();
        } else if (exception instanceof DataIntegrityViolationException) {
            var notification = new Notification(
                    "The SKU is already in use. Please enter another one.");
            notification.setPosition(Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            notification.setDuration(3000);
            notification.open();
        } else {
            // Delegate to Vaadin's default error handler
            throw exception;
        }
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String id) {
        // Update grid selection
        Optional.ofNullable(id)
                .map(UUID::fromString)
                .flatMap(compositionNoteService::findItemById)
                .ifPresentOrElse(grid::select, grid::deselectAll);
        // Show or hide the drawer
        drawer.setCompositionNote(Optional.ofNullable(id)
                .map(UUID::fromString)
                .flatMap(compositionNoteService::findById)
                .orElse(null));
    }

    public static void showCompositionNote(UUID id) {
        UI.getCurrent().navigate(CompositionNoteView.class, id.toString());
    }

    public static void showCompositionNotes() {
        UI.getCurrent().navigate(CompositionNoteView.class);
    }

}
