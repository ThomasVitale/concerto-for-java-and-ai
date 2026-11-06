package com.thomasvitale.mousike.notebook.ui;

import java.time.Duration;
import java.util.List;

import com.thomasvitale.mousike.notebook.Note;
import com.thomasvitale.mousike.notebook.NoteImporter;
import com.thomasvitale.mousike.notebook.NoteKind;
import com.thomasvitale.mousike.notebook.NoteService;
import com.thomasvitale.mousike.notebook.ScoredNote;
import com.thomasvitale.mousike.ui.ExtendedMode;
import com.thomasvitale.mousike.ui.ViewHeader;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;

/**
 * The notebook: notes you write, classified by the decision model.
 */
@Route("notebook")
@RouteAlias("")
@PageTitle("Notebook")
@Menu(title = "Notebook", icon = "vaadin:book", order = 1)
public class NotebookView extends Div {

    private static final int PAGE_SIZE = 60;

    private final NoteService noteService;
    private final NoteImporter importer;
    private final AsyncTaskExecutor executor;
    private final ExtendedMode extendedMode;

    private final TextField search = new TextField();
    private final Select<NoteKind> kindFilter = new Select<>();
    private final RadioButtonGroup<SearchMode> searchMode = new RadioButtonGroup<>();
    private final Div cards = new Div();
    private final Span resultCount = new Span();
    private final KindSpectrum spectrum = new KindSpectrum(kind -> kindFilter.setValue(kind));

    public NotebookView(NoteService noteService, NoteImporter importer, ExtendedMode extendedMode,
            @Qualifier("applicationTaskExecutor") AsyncTaskExecutor executor) {
        this.noteService = noteService;
        this.importer = importer;
        this.executor = executor;
        this.extendedMode = extendedMode;
        addClassName("studio-view");
        addClassName("notebook-view");

        Button newNote = new Button("New note", VaadinIcon.PLUS.create(), e -> openEditor(null));
        newNote.addThemeVariants(ButtonVariant.PRIMARY);
        Button importDocument = new Button("Import document", VaadinIcon.FILE_TEXT_O.create(), e -> openImport());

        // Extended mode: the decision model classifies notes and documents can be imported; plain mode: all by hand
        importDocument.bindVisible(extendedMode.enabled());

        ViewHeader header = new ViewHeader("Your notes", "")
                .bindPurpose(extendedMode.enabled().map(on -> on
                        ? "Jot down what you notice about a chord, a sound or a scene."
                        : "Jot down what you notice about a chord, a sound or a scene, then pick its kind."))
                .withActions(importDocument, newNote);

        search.setPlaceholder("Search your notes…");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.setValueChangeTimeout(300);
        search.addValueChangeListener(e -> refresh());
        search.setWidth("min(28rem, 100%)");

        kindFilter.setItemLabelGenerator(kind -> kind == null ? "All kinds" : kind.label());
        kindFilter.setEmptySelectionCaption("All kinds");
        kindFilter.setEmptySelectionAllowed(true);
        kindFilter.setItems(NoteKind.values());
        kindFilter.addValueChangeListener(e -> refresh());

        searchMode.setItems(SearchMode.values());
        searchMode.setItemLabelGenerator(SearchMode::label);
        searchMode.setValue(SearchMode.WORDS);
        searchMode.addThemeVariants(RadioGroupVariant.AURA_HORIZONTAL);
        searchMode.addClassName("search-mode");
        searchMode.addValueChangeListener(e -> {
            search.setPlaceholder(e.getValue() == SearchMode.MEANING
                    ? "Describe a scene, like a sad goodbye in the rain…"
                    : "Search your notes…");
            refresh();
        });

        resultCount.addClassName("result-count");

        HorizontalLayout toolbar = new HorizontalLayout(search, searchMode, kindFilter, resultCount);
        toolbar.addClassName("notebook-toolbar");
        toolbar.setAlignItems(HorizontalLayout.Alignment.CENTER);
        toolbar.setWidthFull();

        cards.addClassName("note-ledger");

        add(header, toolbar, spectrum, cards);
        refresh();
    }

    private void refresh() {
        cards.removeAll();
        spectrum.update(noteService.countByKind(), kindFilter.getValue());
        boolean byMeaning = searchMode.getValue() == SearchMode.MEANING && !search.getValue().isBlank();
        if (byMeaning) {
            List<ScoredNote> hits = noteService.searchByMeaning(search.getValue(), kindFilter.getValue(), 12);
            resultCount.setText(hits.isEmpty() ? "" : hits.size() + " by meaning");
            if (hits.isEmpty()) {
                cards.add(emptyState("Nothing close to that. Try other words, or write the note you're missing."));
                return;
            }
            hits.forEach(hit -> cards.add(new NoteRow(hit.note(), hit.similarity(), this::openEditor, this::confirmDelete)));
            return;
        }
        List<Note> notes = noteService.browse(search.getValue(), kindFilter.getValue(), PAGE_SIZE);
        resultCount.setText(notes.isEmpty() ? "" : notes.size() + (notes.size() == PAGE_SIZE ? "+ notes" : " notes"));
        if (notes.isEmpty()) {
            cards.add(emptyState(search.getValue().isBlank() && kindFilter.getValue() == null
                    ? "Your notebook is empty. Write your first note."
                    : "No notes with those words. Try searching by meaning."));
            return;
        }
        notes.forEach(note -> cards.add(new NoteRow(note, null, this::openEditor, this::confirmDelete)));
    }

    private void openImport() {
        new ImportDocumentDialog(importer, executor, (fileName, text) -> editor(null).withDocumentText(fileName, text).open()).open();
    }

    private static Paragraph emptyState(String text) {
        Paragraph empty = new Paragraph(text);
        empty.addClassName("empty-state");
        return empty;
    }

    enum SearchMode {
        WORDS("Exact words"), MEANING("By meaning");

        private final String label;

        SearchMode(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }
    }

    private void openEditor(@Nullable Note note) {
        editor(note).open();
    }

    private NoteEditorDialog editor(@Nullable Note note) {
        return new NoteEditorDialog(noteService, executor, note, extendedMode.enabled().peek(), saved -> {
            refresh();
            Duration tagging = saved.taggingDuration();
            String savedAs = "Saved as " + saved.kind().label();
            Notification toast = Notification.show(tagging == null ? savedAs : savedAs + " · %d ms".formatted(tagging.toMillis()),
                    4000, Notification.Position.BOTTOM_START);
            toast.addThemeVariants(NotificationVariant.SUCCESS);
        });
    }

    private void confirmDelete(Note note) {
        ConfirmDialog dialog = new ConfirmDialog("Delete this note?", abbreviate(note.content()), "Delete", e -> {
            noteService.delete(note.requiredId());
            refresh();
        }, "Cancel", e -> { });
        dialog.setConfirmButtonTheme("error primary");
        dialog.open();
    }

    private static String abbreviate(String text) {
        return text.length() > 140 ? text.substring(0, 137) + "…" : text;
    }

}
