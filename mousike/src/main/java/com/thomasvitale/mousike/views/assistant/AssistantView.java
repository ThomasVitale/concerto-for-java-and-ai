package com.thomasvitale.mousike.views.assistant;

import com.thomasvitale.mousike.domain.assistant.ComposerAssistantService;
import com.thomasvitale.mousike.views.MainLayout;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.ListStyleType;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

@PageTitle("Composer Assistant")
@Route(value = "composer-assistant", layout = MainLayout.class)
public class AssistantView extends Main implements HasComponents, HasStyle {

    private OrderedList movieContainer;

    public AssistantView(ComposerAssistantService composerAssistantService) {
        constructUI();
        movieContainer.add(new MovieSceneViewCard(
                "The Doom",
                "Noir",
                "In the dimly lit confines of his apartment, the man, weary from a long day's work, is taken aback by the unexpected presence of a mysterious woman who, with a cold and calculated resolve, raises a gun and fatally shoots him, leaving his lifeless body to crumple in the flickering shadows. (Credits: cottonbro studio)",
                "https://images.pexels.com/photos/7322299/pexels-photo-7322299.jpeg?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=750&q=80", composerAssistantService));
        movieContainer.add(new MovieSceneViewCard(
                "The Mars Cover",
                "Action",
                "Amid the red, rugged landscape of Mars, two intrepid astronauts uncover hidden treasures beneath the alien soil, only to be suddenly pursued by hostile extraterrestrials, forcing them to flee for their lives through the perilous Martian terrain. (Credits: RDNE Stock project)",
                "https://images.pexels.com/photos/8474475/pexels-photo-8474475.jpeg?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=750&q=80", composerAssistantService));
        movieContainer.add(new MovieSceneViewCard(
                "The Loneliest",
                "Dramatic",
                "In the vast, echoing halls of her grand mansion, an elderly woman, enveloped in solitude and melancholy, sits wistfully as the haunting melodies of an old vinyl recorder fill the room with nostalgic tunes from a bygone era. (Credits: cottonbro studio)",
                "https://images.pexels.com/photos/8862291/pexels-photo-8862291.jpeg?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=750&q=80", composerAssistantService));
    }

    private void constructUI() {
        addClassNames("image-gallery-view");
        addClassNames(MaxWidth.SCREEN_LARGE, Margin.Horizontal.AUTO, Padding.Bottom.LARGE, Padding.Horizontal.LARGE);

        HorizontalLayout container = new HorizontalLayout();
        container.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN);

        VerticalLayout headerContainer = new VerticalLayout();
        H2 header = new H2("Plan a Composition");
        header.addClassNames(Margin.Bottom.NONE, Margin.Top.XLARGE, FontSize.XXXLARGE);
        Paragraph description = new Paragraph("Start planning your next soundtrack");
        description.addClassNames(Margin.Bottom.XLARGE, Margin.Top.NONE, TextColor.SECONDARY);
        headerContainer.add(header, description);

        movieContainer = new OrderedList();
        movieContainer.addClassNames(Gap.MEDIUM, Display.GRID, ListStyleType.NONE, Margin.NONE, Padding.NONE);

        container.add(headerContainer);
        add(container, movieContainer);
    }

}
