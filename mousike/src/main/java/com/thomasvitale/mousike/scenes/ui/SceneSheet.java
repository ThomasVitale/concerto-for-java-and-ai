package com.thomasvitale.mousike.scenes.ui;

import java.util.function.Consumer;

import com.thomasvitale.mousike.scenes.Scene;
import com.thomasvitale.mousike.scenes.SceneMarker;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.jspecify.annotations.Nullable;

/**
 * A scene rendered like a plan: title and description on top, the director's markers down the timeline.
 */
public class SceneSheet extends Div {

    public SceneSheet(Scene scene) {
        this(scene, null);
    }

    public SceneSheet(Scene scene, @Nullable Consumer<Scene> onDelete) {
        addClassName("scene-sheet");

        H3 title = new H3(scene.title());
        title.addClassName("scene-title");
        Span meta = new Span(scene.genre() == null ? scene.medium().label() : scene.medium().label() + ", " + scene.genre());
        meta.addClassName("scene-meta");
        Div heading = new Div(title, meta);
        if (scene.production() != null) {
            Span production = new Span(scene.production());
            production.addClassName("scene-production");
            heading.addComponentAsFirst(production);
        }
        heading.addClassName("scene-heading");

        Div head = new Div(heading);
        head.addClassName("scene-head");
        if (onDelete != null) {
            Button delete = new Button(VaadinIcon.TRASH.create(), e -> onDelete.accept(scene));
            delete.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
            delete.setAriaLabel("Delete scene");
            delete.setTooltipText("Delete");
            head.add(delete);
        }

        Paragraph description = new Paragraph(scene.description());
        description.addClassName("scene-description");

        Div timeline = new Div();
        timeline.addClassName("scene-timeline");
        for (SceneMarker marker : scene.markers()) {
            Span time = new Span(marker.timeMarker());
            time.addClassNames("studio-timecode", "marker-time-mark");
            Span comment = new Span(marker.comment());
            comment.addClassName("marker-comment-text");
            Div row = new Div(time, comment);
            row.addClassName("timeline-row");
            timeline.add(row);
        }
        if (scene.markers().isEmpty()) {
            Span none = new Span("No markers yet.");
            none.addClassName("scene-meta");
            timeline.add(none);
        }

        add(head, description, timeline);
    }

}
