package com.thomasvitale.mousike.ui;

import java.util.List;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.checkbox.Switch;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.signals.Signal;

/**
 * Application shell: a light "scoring studio" with a drawer for navigation.
 * Views register themselves through {@code @Menu}; icons use the {@code vaadin:name} form.
 */
@Layout
public class MainLayout extends AppLayout implements BeforeEnterObserver, AfterNavigationObserver {

    private final H1 viewTitle = new H1();
    private final ExtendedMode extendedMode;

    public MainLayout(ExtendedMode extendedMode) {
        this.extendedMode = extendedMode;
        addClassName("studio-layout");
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
        // Switched off while on an extended view: the view does not exist in the plain app
        Signal.effect(this, () -> {
            if (!extendedMode.enabled().get() && getContent() != null && getContent().getClass().isAnnotationPresent(ExtendedOnly.class)) {
                getUI().ifPresent(ui -> ui.access(() -> ui.navigate("notebook")));
            }
        });
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");
        viewTitle.addClassName("studio-view-title");
        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Div brandMark = new Div();
        brandMark.addClassName("studio-brand-mark");
        for (String hue : new String[] { "harmony", "melody", "rhythm", "instrument", "style" }) {
            Span bar = new Span();
            bar.addClassName("studio-brand-bar");
            bar.getElement().setAttribute("data-kind", hue);
            brandMark.add(bar);
        }
        Span appName = new Span("Mousike");
        appName.addClassName("studio-brand-name");
        Span tagline = new Span("Composer's Studio");
        tagline.addClassName("studio-brand-tagline");
        Div brand = new Div(brandMark, new Div(appName, tagline));
        brand.addClassName("studio-brand");
        Header header = new Header(brand);
        header.addClassName("studio-sidebar-header");

        addToDrawer(header, new Scroller(createNavigation()), createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.addClassName("studio-nav");
        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();
        menuEntries.forEach(entry -> {
            SideNavItem item = new SideNavItem(entry.title(), entry.path());
            if (entry.icon() != null && entry.icon().startsWith("vaadin:")) {
                item.setPrefixComponent(new Icon("vaadin", entry.icon().substring("vaadin:".length())));
            }
            if (entry.menuClass() != null && entry.menuClass().isAnnotationPresent(ExtendedOnly.class)) {
                item.bindVisible(extendedMode.enabled());
            }
            nav.addItem(item);
        });
        return nav;
    }

    private Footer createFooter() {
        Footer footer = new Footer();
        footer.addClassName("studio-sidebar-footer");
        Switch extended = new Switch("Extended");
        extended.addClassName("studio-extended-switch");
        extended.bindValue(extendedMode.enabled(), extendedMode.enabled()::set);
        footer.add(extended);
        Span note = new Span("Every idea at hand. You compose.");
        Span credit = new Span("Java, Spring, and a piano.");
        credit.addClassName("studio-footer-credit");
        footer.add(note, credit);
        return footer;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!extendedMode.enabled().peek() && event.getNavigationTarget().isAnnotationPresent(ExtendedOnly.class)) {
            event.forwardTo("notebook");
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        viewTitle.setText(MenuConfiguration.getPageHeader(getContent()).orElse(""));
    }

}
