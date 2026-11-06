package com.thomasvitale.mousike.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.signals.Signal;

/**
 * Title and one-sentence purpose, shared by every view.
 */
public class ViewHeader extends Div {

    private final Div actions = new Div();
    private final Paragraph purposeParagraph;

    public ViewHeader(String title, String purpose) {
        addClassName("view-header");
        H2 heading = new H2(title);
        heading.addClassName("view-title");
        purposeParagraph = new Paragraph(purpose);
        purposeParagraph.addClassName("view-purpose");
        Div copy = new Div(heading, purposeParagraph);
        copy.addClassName("view-header-copy");
        actions.addClassName("view-header-actions");
        add(copy, actions);
    }

    public ViewHeader withActions(Component... components) {
        actions.add(components);
        return this;
    }

    /** Lets the purpose follow application state, e.g. a mode switch. */
    public ViewHeader bindPurpose(Signal<String> purpose) {
        purposeParagraph.bindText(purpose);
        return this;
    }

}
