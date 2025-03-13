package com.thomasvitale.mousike.demo;

import java.util.List;

import com.thomasvitale.mousike.domain.compositionnote.CompositionNote;
import com.thomasvitale.mousike.domain.compositionnote.CompositionNoteService;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class TestDataLoader {

    private final CompositionNoteService compositionNoteService;

    TestDataLoader(CompositionNoteService compositionNoteService) {
        this.compositionNoteService = compositionNoteService;
    }

    @EventListener(ApplicationReadyEvent.class)
    void load() {
        if (compositionNoteService.count() > 0) {
            return;
        }
        var compositionNotes = List.of(
                new CompositionNote(null, CompositionNote.Type.INSTRUMENT,
                        "Piano: Evokes intimacy, passion, warmth, and introspection. Can convey a wide range of emotions from melancholy to joyous depending on how it's played. A good fit for dramatic and sad scenes."),
                new CompositionNote(null, CompositionNote.Type.INSTRUMENT,
                        "Strings: Lush and emotive, strings can evoke romanticism, drama, and tension. They add depth and richness to a composition and are often used for emotional impact in film scores."),
                new CompositionNote(null, CompositionNote.Type.INSTRUMENT,
                        "Brass: Powerful and majestic, brass instruments can convey grandeur, action, heroism, and triumph. They are often used for epic, action, adventure, and triumphant moments in film music."),
                new CompositionNote(null, CompositionNote.Type.INSTRUMENT,
                        "Cello: Rich and resonant, the cello can evoke depth and emotion. It's often used for melodic lines that convey romance, passion, melancholy, longing, and introspection in film scores."),
                new CompositionNote(null, CompositionNote.Type.INSTRUMENT,
                        "Percussions: Percussions can add energy, tension, and excitement to film music. A good fit for action and fight scenes to build intensity."),
                new CompositionNote(null, CompositionNote.Type.INSTRUMENT,
                        "Drones: Drones create a sense of tension, suspense, and unease. A good fit in suspenseful or mysterious scenes, noir, crime, mystical, and eerie atmospheres."),
                new CompositionNote(null, CompositionNote.Type.INSTRUMENT,
                        "Harp: Ethereal and magical, the harp can evoke a sense of wonder, mystery, and enchantment. It's often used to add a celestial quality to film scores and can convey moments of beauty and serenity but also tension."),
                new CompositionNote(null, CompositionNote.Type.HARMONY,
                        "A chord progression for epic or action scenes: i VI III VII"),
                new CompositionNote(null, CompositionNote.Type.HARMONY,
                        "A chord progression for mystery and noir scenes: i II v II"),
                new CompositionNote(null, CompositionNote.Type.HARMONY,
                        "A chord progression for dramatic and melancholic scenes: i VI III VII")
        );
        compositionNotes.forEach(compositionNoteService::save);
    }

}
