package com.thomasvitale.mousike.notebook.ui;

import java.time.Duration;
import java.util.Locale;
import java.util.function.Consumer;

import com.thomasvitale.mousike.notebook.Note;
import com.thomasvitale.mousike.ui.ModelBadge;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.jspecify.annotations.Nullable;

/**
 * One line of the ledger: the composer's words in the body, the model's pencil marks in the margins.
 */
class NoteRow extends Div {

    NoteRow(Note note, @Nullable Double similarity, Consumer<Note> onEdit, Consumer<Note> onDelete) {
        addClassName("note-row");
        getElement().setAttribute("data-kind", note.kind().name().toLowerCase(Locale.ROOT));

        // Left margin: the kind, as a pencil mark
        Span kind = new Span(note.kind().label());
        kind.addClassName("pencil-kind");
        Div margin = new Div(kind);
        margin.addClassName("note-margin");
        if (similarity != null) {
            Span match = new Span("%.0f%% match".formatted(similarity * 100));
            match.addClassName("similarity-mark");
            margin.addComponentAsFirst(match);
        }

        // Body: the note itself
        Paragraph content = new Paragraph(note.content());
        content.addClassName("note-body-text");
        Div body = new Div(content);
        body.addClassName("note-body");

        Button edit = new Button(VaadinIcon.PENCIL.create(), e -> onEdit.accept(note));
        edit.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
        edit.setAriaLabel("Edit note");
        edit.setTooltipText("Edit");
        Button delete = new Button(VaadinIcon.TRASH.create(), e -> onDelete.accept(note));
        delete.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
        delete.setAriaLabel("Delete note");
        delete.setTooltipText("Delete");
        Div actions = new Div(edit, delete);
        actions.addClassName("note-actions");
        Div side = new Div(actions);
        side.addClassName("note-side");
        // Classified by the decision model: say which one and how fast. Chosen by hand: nothing to credit
        String taggedBy = note.taggedBy();
        Duration tagging = note.taggingDuration();
        if (taggedBy != null && tagging != null) {
            side.add(new ModelBadge(taggedBy, tagging));
        }

        add(margin, body, side);
    }

}
