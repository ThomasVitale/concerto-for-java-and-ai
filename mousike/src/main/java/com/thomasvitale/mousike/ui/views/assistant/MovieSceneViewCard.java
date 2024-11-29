package com.thomasvitale.mousike.ui.views.assistant;

import com.thomasvitale.mousike.domain.assistant.ComposerAssistantService;
import com.thomasvitale.mousike.domain.assistant.CompositionPlan;
import com.thomasvitale.mousike.domain.assistant.SceneToScore;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Overflow;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import com.vaadin.flow.theme.lumo.LumoUtility.Width;

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
