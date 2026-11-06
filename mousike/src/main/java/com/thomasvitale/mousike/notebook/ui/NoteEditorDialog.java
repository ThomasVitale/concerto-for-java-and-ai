package com.thomasvitale.mousike.notebook.ui;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.thomasvitale.mousike.notebook.Classification;
import com.thomasvitale.mousike.notebook.Classification.Classified;
import com.thomasvitale.mousike.notebook.Classification.NeedsReview;
import com.thomasvitale.mousike.notebook.KindGuess;
import com.thomasvitale.mousike.notebook.Note;
import com.thomasvitale.mousike.notebook.NoteKind;
import com.thomasvitale.mousike.notebook.NoteService;
import com.thomasvitale.mousike.ui.ErrorToast;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.task.AsyncTaskExecutor;

/**
 * Write a note. In extended mode the decision model classifies it and there is no kind picker, unless the model is
 * unsure or unavailable: then the picker appears and the composer decides. In plain mode the composer always picks
 * the kind, as before generative AI.
 */
class NoteEditorDialog extends Dialog {

    private static final Logger log = LoggerFactory.getLogger(NoteEditorDialog.class);

    private final NoteService noteService;
    private final AsyncTaskExecutor executor;
    private final @Nullable Note existing;
    private final Consumer<Note> onSaved;
    private boolean modelClassifies;

    private final TextArea content = new TextArea();
    private final Select<NoteKind> kind = new Select<>();
    private final Button cancel = new Button("Cancel", e -> close());
    private final Button save = new Button();
    private final Span hint = new Span();

    NoteEditorDialog(NoteService noteService, AsyncTaskExecutor executor, @Nullable Note existing, boolean extended,
            Consumer<Note> onSaved) {
        this.noteService = noteService;
        this.executor = executor;
        this.existing = existing;
        this.onSaved = onSaved;
        this.modelClassifies = extended;

        addClassName("note-editor");
        setHeaderTitle(existing == null ? "New note" : "Edit note");
        setWidth("min(40rem, 92vw)");
        setCloseOnOutsideClick(false);

        content.setLabel("What's on your mind?");
        content.setPlaceholder("For example: the tin whistle works great for cheerful fairytale scenes.");
        content.setWidthFull();
        content.setMinHeight("10rem");
        content.setMaxLength(2000);
        content.setValueChangeMode(ValueChangeMode.EAGER);
        content.addValueChangeListener(e -> updateSave());

        kind.setLabel("Kind");
        kind.setPlaceholder("Pick one…");
        kind.setItems(NoteKind.values());
        kind.setItemLabelGenerator(NoteKind::label);
        kind.setRequiredIndicatorVisible(true);
        kind.setVisible(!extended);
        kind.addValueChangeListener(e -> updateSave());

        hint.setText(extended ? "The kind is picked for you when you save." : "Pick a kind, then save.");
        hint.addClassName("note-editor-hint");

        VerticalLayout body = new VerticalLayout(content, kind, hint);
        body.setPadding(false);
        body.setSpacing(true);
        add(body);

        cancel.addThemeVariants(ButtonVariant.TERTIARY);
        save.setText("Save");
        save.addThemeVariants(ButtonVariant.PRIMARY);
        save.setEnabled(false);
        save.addClickShortcut(Key.ENTER, KeyModifier.META);
        save.addClickListener(e -> saveNote());
        getFooter().add(cancel, save);

        if (existing != null) {
            content.setValue(existing.content());
            kind.setValue(existing.kind());
        }
        content.focus();
    }

    /** Starts from text Docling read out of a document, for the composer to check before saving. */
    NoteEditorDialog withDocumentText(String fileName, String text) {
        content.setValue(text);
        hint.setText("Here's what we read from %s. Fix anything that looks off, then save.".formatted(fileName));
        return this;
    }

    private void updateSave() {
        save.setEnabled(!content.getValue().isBlank() && (modelClassifies || kind.getValue() != null));
    }

    private void saveNote() {
        String text = content.getValue();
        if (text.isBlank()) {
            return;
        }
        UI ui = UI.getCurrent();
        NoteKind chosen = kind.getValue();
        save.setEnabled(false);
        content.setReadOnly(true);
        kind.setReadOnly(true);
        cancel.setEnabled(false);
        setCloseOnEsc(false);
        save.setText("Saving…");
        boolean askModel = modelClassifies;
        if (askModel) {
            hint.setText("Working out the kind…");
        }

        CompletableFuture
                .supplyAsync(() -> persist(text, chosen, askModel), executor)
                .whenComplete((classification, failure) -> ui.access(() -> {
                    if (failure != null) {
                        log.error("Could not save note", failure);
                        if (askModel) {
                            askForKind("Couldn't work out the kind this time. Pick one yourself.");
                            return;
                        }
                        ErrorToast.show("Could not save the note", failure);
                        unlock();
                        return;
                    }
                    switch (classification) {
                        case Classified classified -> {
                            onSaved.accept(classified.note());
                            close();
                        }
                        case NeedsReview review -> askForKind("Not sure if this is %s. Which one is it?"
                                .formatted(review.candidates().stream().map(KindGuess::describe).collect(Collectors.joining(" or "))));
                    }
                }));
    }

    private Classification persist(String text, @Nullable NoteKind chosen, boolean askModel) {
        if (askModel) {
            return existing == null ? noteService.create(text) : noteService.revise(existing.requiredId(), text);
        }
        NoteKind kindChosen = Objects.requireNonNull(chosen, "kind");
        return new Classified(existing == null
                ? noteService.create(text, kindChosen)
                : noteService.revise(existing.requiredId(), text, kindChosen));
    }

    /** The model can't decide or isn't there: the composer chooses, and the next save keeps their choice. */
    private void askForKind(String reason) {
        modelClassifies = false;
        kind.setVisible(true);
        hint.setText(reason);
        unlock();
        kind.focus();
    }

    private void unlock() {
        content.setReadOnly(false);
        kind.setReadOnly(false);
        cancel.setEnabled(true);
        setCloseOnEsc(true);
        save.setText("Save");
        updateSave();
    }

}
