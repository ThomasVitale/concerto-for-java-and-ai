package com.thomasvitale.mousike.ui;

import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;

import org.springframework.stereotype.Component;

/**
 * The app's two versions, for the demo: off is the plain app, where the composer does everything by hand;
 * on is the extended app, where models help (classifying notes, importing documents, ...). One switch per browser
 * session, shown in the sidebar; views bind to {@link #enabled()} to follow it.
 */
@Component
@VaadinSessionScope
public class ExtendedMode {

    private final ValueSignal<Boolean> enabled = new ValueSignal<>(true);

    public ValueSignal<Boolean> enabled() {
        return enabled;
    }

}
