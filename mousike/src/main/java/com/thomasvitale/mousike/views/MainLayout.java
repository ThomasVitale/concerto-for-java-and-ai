package com.thomasvitale.mousike.views;

import java.util.Arrays;
import java.util.List;

import com.thomasvitale.mousike.views.assistant.AssistantView;
import com.thomasvitale.mousike.views.compositionnotes.CompositionNoteView;
import com.thomasvitale.mousike.views.directornotes.DirectorNoteAiFormView;
import com.thomasvitale.mousike.views.directornotes.DirectorNoteFormView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;

import org.springframework.core.env.Environment;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private H1 viewTitle;

    public MainLayout(Environment environment) {
        setPrimarySection(Section.DRAWER);
        addDrawerContent(Arrays.asList(environment.getActiveProfiles()));
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent(List<String> activeProfiles) {
        Span appName = new Span("Mousike");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation(activeProfiles));

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation(List<String> activeProfiles) {
        SideNav nav = new SideNav();

        if (activeProfiles.contains("plain")) {
            nav.addItem(new SideNavItem("Composition Notes", CompositionNoteView.class, LineAwesomeIcon.GUITAR_SOLID.create()));
            nav.addItem(new SideNavItem("Director Notes", DirectorNoteFormView.class, LineAwesomeIcon.FILM_SOLID.create()));
        } else {
            nav.addItem(new SideNavItem("Composition Notes", CompositionNoteView.class, LineAwesomeIcon.MAGIC_SOLID.create()));
            nav.addItem(new SideNavItem("Director Notes", DirectorNoteAiFormView.class, LineAwesomeIcon.MAGIC_SOLID.create()));
            nav.addItem(new SideNavItem("Assistant", AssistantView.class, LineAwesomeIcon.MAGIC_SOLID.create()));
        }

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
