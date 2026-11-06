package com.thomasvitale.mousike.chords.ui.internal;

import com.thomasvitale.mousike.chords.ChordProgression;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Plays a progression in the browser: a General MIDI piano from a self-hosted soundfont, chords highlighted as they sound.
 * The server only hands over the MIDI bytes; synthesis happens in the browser.
 */
@Tag("mousike-chord-progression-player")
@JsModule("./chord-progression-player.ts")
@NpmPackage(value = "spessasynth_lib", version = "4.4.1")
@NpmPackage(value = "spessasynth_core", version = "4.4.2")
public class ChordProgressionPlayer extends Component {

    public ChordProgressionPlayer(ChordProgression progression) {
        getElement().setProperty("src", "/progressions/" + progression.requiredId() + ".mid");
        getElement().setPropertyList("chords", progression.chordList());
        getElement().setProperty("secondsPerChord", progression.beatsPerChord() * 60.0 / progression.tempoBpm());
        getElement().setProperty("seconds", progression.seconds());
        getElement().setAttribute("aria-label", "Chord progression: " + progression.title());
    }

}
