package com.thomasvitale.mousike.compositionplan.ui;

import java.net.URI;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.dom.Element;

/**
 * Samples a Kontakt preset the way Kontakt's Browser does: the short Ogg clip the library ships, streamed by the
 * library. Nothing loads until the composer presses play. The audio element's own events drive the button, so it
 * always tells what the browser is doing: one sample at a time, and back to "Sample" if the library can't be reached.
 */
class PresetSample extends Span {

    private final Element audio = new Element("audio");
    private final Button button = new Button();
    private boolean playing;

    PresetSample(String preset, URI preview) {
        addClassName("composition-sample");
        audio.setAttribute("src", preview.toString());
        audio.setAttribute("preload", "none");
        audio.addEventListener("play", event -> show(true));
        List.of("pause", "ended", "error").forEach(stopped -> audio.addEventListener(stopped, event -> show(false)));
        getElement().appendChild(audio);

        button.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
        button.setAriaLabel("Sample " + preset);
        button.addClickListener(event -> {
            if (playing) {
                audio.callJsFunction("pause");
            }
            else {
                // load() starts over from the top, and retries after a failed attempt
                audio.executeJs("document.querySelectorAll('audio').forEach(other => other.pause()); this.load(); this.play();");
            }
        });
        show(false);
        add(button);
    }

    private void show(boolean playing) {
        this.playing = playing;
        button.setIcon(playing ? VaadinIcon.STOP.create() : VaadinIcon.PLAY.create());
        button.setText(playing ? "Stop" : "Sample");
    }

}
