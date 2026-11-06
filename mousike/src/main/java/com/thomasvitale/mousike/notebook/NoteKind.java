package com.thomasvitale.mousike.notebook;

public enum NoteKind {

    HARMONY("Harmony", "Chord progressions, cadences, modulations, voicings"),
    MELODY("Melody", "Melodic ideas, motifs, themes, phrasing"),
    RHYTHM("Rhythm", "Grooves, ostinati, tempo and metre ideas"),
    INSTRUMENT("Instrument", "A specific instrument or preset and what it is good for"),
    ORCHESTRATION("Orchestration", "How instruments are combined, layered and voiced across an ensemble"),
    ARRANGEMENT("Arrangement", "How a piece unfolds over time: sections, builds, transitions, dynamics"),
    SOUND_DESIGN("Sound design", "Textures, effects, extended techniques, processing"),
    STYLE("Style", "A genre, era, composer or film/game/show reference"),
    TECHNIQUE("Technique", "A compositional or performance technique, theory or workflow tip");

    private final String label;
    private final String description;

    NoteKind(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

}
