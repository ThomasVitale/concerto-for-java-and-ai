package com.thomasvitale.mousike.compositionnote.ui;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

class QuestionAnsweringForm extends Composite<VerticalLayout> {

    private final TextField questionField;
    private final Div answerContainer;
    private Markdown answerMarkdown;

    QuestionAnsweringForm() {
        // Create components
        questionField = new TextField("Question");
        questionField.setPlaceholder("Ask a question about composition notes...");
        questionField.setWidthFull();

        answerContainer = new Div();
        answerContainer.setWidthFull();
        answerContainer.getStyle().set("min-height", "200px");
        answerContainer.getStyle().set("max-height", "400px");
//        answerContainer.getStyle().set("overflow", "auto");
        answerContainer.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        answerContainer.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
//        answerContainer.getStyle().set("padding", "var(--lumo-space-s)");
        answerContainer.getStyle().set("word-wrap", "break-word");
        answerContainer.getStyle().set("overflow-wrap", "break-word");

        // Layout form
        var layout = getContent();
        layout.add(questionField);
        layout.add(answerContainer);
        layout.setPadding(false);
        layout.setSpacing(true);
    }

    public String getQuestion() {
        return questionField.getValue();
    }

    public void setAnswer(String answer) {
        answerContainer.removeAll();
        if (answer != null && !answer.isEmpty()) {
            answerMarkdown = new Markdown(answer);
            answerContainer.add(answerMarkdown);
        }
    }

    public void clearAnswer() {
        answerContainer.removeAll();
        answerMarkdown = null;
    }

    public boolean hasQuestion() {
        return questionField.getValue() != null && !questionField.getValue().trim().isEmpty();
    }

}
