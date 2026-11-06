package com.thomasvitale.mousike.ui;

import com.vaadin.flow.component.html.Div;

/**
 * A five-line staff used as a section divider.
 */
public class StaffDivider extends Div {

    public StaffDivider() {
        addClassName("staff-divider");
        getElement().setAttribute("aria-hidden", "true");
        for (int i = 0; i < 5; i++) {
            add(new Div());
        }
    }

}
