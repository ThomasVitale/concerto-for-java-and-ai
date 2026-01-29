package com.thomasvitale.mousike.compositionnote.ui;

import com.thomasvitale.mousike.compositionnote.domain.CompositionNoteService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;

class AddQuestionAnsweringDialog extends Dialog {

    private final CompositionNoteService compositionNoteService;
    private final QuestionAnsweringForm form;

    AddQuestionAnsweringDialog(CompositionNoteService compositionNoteService) {
        this.compositionNoteService = compositionNoteService;

        // Create components
        form = new QuestionAnsweringForm();

        var askButton = new Button("Ask", e -> askQuestion());
        askButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        askButton.addClickShortcut(Key.ENTER);

        var closeButton = new Button("Close", e -> close());
        closeButton.addClickShortcut(Key.ESCAPE);

        // Layout dialog
        setHeaderTitle("Ask a Question");
        add(form);
        getFooter().add(closeButton, askButton);
        setWidth("600px");
    }

    private void askQuestion() {
        if (!form.hasQuestion()) {
            Notification.show("Please enter a question");
            return;
        }

        try {
            String question = form.getQuestion();
            String answer = compositionNoteService.answer(question);
            form.setAnswer(answer);
        } catch (Exception e) {
            Notification.show("Error getting answer: " + e.getMessage());
        }
    }

}
