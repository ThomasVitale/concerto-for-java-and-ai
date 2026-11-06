package com.thomasvitale.mousike.notebook.ui;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import com.thomasvitale.mousike.notebook.KindCount;
import com.thomasvitale.mousike.notebook.NoteKind;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import org.jspecify.annotations.Nullable;

/**
 * The notebook as a spectrum: one coloured band per kind, as wide as its share of the notes.
 * Click a band to filter by that kind; click it again to clear.
 */
class KindSpectrum extends Div {

    private final Div bar = new Div();
    private final Div legend = new Div();
    private final Consumer<@Nullable NoteKind> onSelect;
    private @Nullable NoteKind selected;

    KindSpectrum(Consumer<@Nullable NoteKind> onSelect) {
        this.onSelect = onSelect;
        addClassName("spectrum");
        bar.addClassName("spectrum-bar");
        bar.getElement().setAttribute("role", "group");
        bar.getElement().setAttribute("aria-label", "Notes by kind");
        legend.addClassName("spectrum-legend");
        add(bar, legend);
    }

    void update(List<KindCount> counts, @Nullable NoteKind selected) {
        this.selected = selected;
        bar.removeAll();
        legend.removeAll();
        setClassName("spectrum-filtered", selected != null);
        long total = counts.stream().mapToLong(KindCount::total).sum();
        for (NoteKind kind : NoteKind.values()) {
            long count = counts.stream().filter(c -> c.kind() == kind).mapToLong(KindCount::total).sum();
            if (count == 0) {
                continue;
            }
            Span band = new Span();
            band.addClassName("spectrum-band");
            band.getElement().setAttribute("data-kind", kind.name().toLowerCase(Locale.ROOT));
            band.getStyle().set("flex-grow", String.valueOf(count));
            band.getElement().setAttribute("title", "%s: %d %s".formatted(kind.label(), count, count == 1 ? "note" : "notes"));
            band.getElement().setAttribute("role", "button");
            band.getElement().setAttribute("tabindex", "0");
            band.setClassName("spectrum-band-selected", kind == selected);
            band.addClickListener(e -> toggle(kind));
            band.getElement().addEventListener("keydown", e -> toggle(kind))
                    .setFilter("event.key === 'Enter' || event.key === ' '")
                    .preventDefault();

            Span item = new Span();
            item.addClassName("spectrum-legend-item");
            item.getElement().setAttribute("data-kind", kind.name().toLowerCase(Locale.ROOT));
            item.setClassName("spectrum-legend-selected", kind == selected);
            Span dot = new Span();
            dot.addClassName("spectrum-dot");
            Span label = new Span(kind.label());
            Span number = new Span(String.valueOf(count));
            number.addClassName("spectrum-count");
            item.add(dot, label, number);
            item.addClickListener(e -> toggle(kind));
            legend.add(item);

            bar.add(band);
        }
        bar.setVisible(total > 0);
        legend.setVisible(total > 0);
    }

    private void toggle(NoteKind kind) {
        onSelect.accept(kind == selected ? null : kind);
    }

}
