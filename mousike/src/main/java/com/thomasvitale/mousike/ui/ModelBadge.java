package com.thomasvitale.mousike.ui;

import java.time.Duration;

import com.vaadin.flow.component.html.Span;

/**
 * Small provenance label: which model did the work and how long it took, always on this machine.
 */
public class ModelBadge extends Span {

    public ModelBadge(String model, Duration duration) {
        addClassName("model-badge");
        Span dot = new Span();
        dot.addClassName("model-badge-dot-local");
        Span text = new Span("%s in %s, locally".formatted(model, duration(duration)));
        add(dot, text);
        getElement().setAttribute("title", "Model that produced this result");
    }

    /** "340 ms" below a second, "2.4 s" above, "1 min 12 s" above a minute. */
    public static String duration(Duration duration) {
        if (duration.compareTo(Duration.ofSeconds(1)) < 0) {
            return duration.toMillis() + " ms";
        }
        if (duration.compareTo(Duration.ofMinutes(1)) < 0) {
            return "%.1f s".formatted(duration.toMillis() / 1000.0);
        }
        return "%d min %d s".formatted(duration.toMinutes(), duration.toSecondsPart());
    }

}
