package com.thomasvitale.mousike.views.assistant;

import com.thomasvitale.mousike.domain.assistant.ComposerAssistantService;
import com.thomasvitale.mousike.domain.assistant.CompositionPlan;
import com.thomasvitale.mousike.domain.assistant.SceneToScore;
import com.thomasvitale.mousike.domain.compositionnote.CompositionNote;
import com.thomasvitale.mousike.views.compositionnotes.CompositionNoteView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.theme.lumo.LumoUtility.*;

import java.util.List;

public class MovieSceneViewCard extends ListItem {

    public MovieSceneViewCard(String movie, String genre, String sceneDescription, String url, ComposerAssistantService composerAssistantService) {
        addClassNames(Background.CONTRAST_5, Display.FLEX, FlexDirection.COLUMN, AlignItems.START, Padding.MEDIUM,
                BorderRadius.LARGE);

        Div div = new Div();
        div.addClassNames(Background.CONTRAST, Display.FLEX, AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Bottom.MEDIUM, Overflow.HIDDEN, BorderRadius.MEDIUM, Width.FULL);
        div.setHeight("160px");

        Image image = new Image();
        image.setWidth("100%");
        image.setSrc(url);
        image.setAlt(sceneDescription);

        div.add(image);

        Span header = new Span();
        header.addClassNames(FontSize.XLARGE, FontWeight.SEMIBOLD);
        header.setText(movie);

        Span subtitle = new Span();
        subtitle.addClassNames(FontSize.SMALL, TextColor.SECONDARY);
        subtitle.setText(genre);

        Paragraph description = new Paragraph(sceneDescription);
        description.addClassName(Margin.Vertical.MEDIUM);

//        Span badge = new Span();
//        badge.getElement().setAttribute("theme", "badge");
//        badge.setText("Movie");

        Button button = new Button();
        button.setText("Plan");

        button.addClickListener(e -> {
            var sceneToScore = new SceneToScore(movie, genre, sceneDescription);
            var compositionPlan = composerAssistantService.plan(sceneToScore);

            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Composition Plan");
            dialog.add(dialogLayout(compositionPlan));

            System.out.println(compositionPlan);

            dialog.open();
        });

        add(div, header, subtitle, description, button);
    }

    private VerticalLayout dialogLayout(CompositionPlan compositionPlan) {
        Div div = new Div();

        H3 title = new H3("Chord Progressions");
        div.add(title);

        compositionPlan.chordProgressions().forEach(chord ->
                div.add(new Paragraph(chord))
        );

        H3 strategy = new H3("Strategy");
        div.add(strategy);

        compositionPlan.compositionStrategySteps().forEach(step ->
                div.add(new Paragraph(step))
        );

        return new VerticalLayout(div);
    }
}
