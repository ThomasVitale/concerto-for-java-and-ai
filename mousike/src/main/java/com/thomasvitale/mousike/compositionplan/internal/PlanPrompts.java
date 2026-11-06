package com.thomasvitale.mousike.compositionplan.internal;

import java.util.List;
import java.util.stream.Collectors;

import com.thomasvitale.mousike.scenes.Scene;

/**
 * What the planner tells the model at each step. Keep the numbered steps of the instrument search with "write the list"
 * last: without them, the model skips the searches and makes everything up.
 */
public final class PlanPrompts {

    public static final String HARMONY = """
            You suggest chord progressions for a scene, for a composer who writes the music themselves. You get the scene and \
            what the composer already has. Suggest other progressions for its mood and genre, each different from what they \
            have, written as chord symbols like 'Dm Bb F C', with the key.""";

    private static final String LIST_FORMAT = """
            Write the list only after those searches, from what they returned, one line per sound:
            - role: theme; sound: solo cello; preset id: 152; why: carries the lonely melody
            Exactly three lines. Start with the instruments the notes name for this kind of scene, and recommend your own \
            when the notes do not cover what the scene needs. The role is the sound's job in the music ('theme', 'low pulse', 'texture'), not a time. The \
            sound is the instrument in plain words ('solo cello'), never a preset's name or id. The why says what it does \
            in this scene, in one sentence, without naming a preset or the scene's title.""";

    public static final String INSTRUMENTS = """
            You choose three instruments for a scene's music, for a composer who writes the music themselves. You never write \
            melodies; you start from the composer's own ideas and pick instruments they own.
            Work in this order:
            1. Read the scene and the director's markers.
            2. Always search the composer's notebook first, several times, with different words, for the instruments and \
            the orchestration the scene calls for.
            3. For each sound you need, search the instrument library for that sound, e.g. 'solo cello legato', \
            'low brass', 'evolving pad'. Take a preset that really is that sound, and only with an id the search \
            returned. When none fits, write 0 as its preset id.
            4. Then write the list.
            """ + LIST_FORMAT;

    /** The same step when the library is offline: no library step, so the model is not told about tools it does not have. */
    public static final String INSTRUMENTS_LIBRARY_OFFLINE = """
            You choose three instruments for a scene's music, for a composer who writes the music themselves. You never write \
            melodies; you start from the composer's own ideas.
            Work in this order:
            1. Read the scene and the director's markers.
            2. Always search the composer's notebook first, several times, with different words, for the instruments and \
            the orchestration the scene calls for.
            3. Then write the list. The instrument library is offline today, so write 0 as every preset id.
            """ + LIST_FORMAT;

    public static final String PALETTE = """
            You turn a list of instruments into structured data, copying it line by line: add nothing, change no id. Each line \
            labels its role, sound, preset id and why. A preset id is a number; use 0 when the list gives none.""";

    public static final String ARC = """
            You suggest the orchestration and arrangement of a scene's music, for a composer who writes the music themselves. \
            You get the scene, the sounds and chord progressions already chosen, and the composer's notes on orchestration \
            and arrangement. Write the idea of the music in two sentences. Then write one moment for each of the director's \
            time markers, in order, with the time marker exactly as written: which of the sounds you got play there, how \
            they enter and how the music builds, following the notes where they fit and adding your own ideas where they do not, in plain words. You never write \
            melodies.""";

    private PlanPrompts() {
    }

    /** The scene as every step reads it. */
    static String describe(Scene scene) {
        String markers = scene.markers().stream()
                .map(marker -> "- " + marker.timeMarker() + ": " + marker.comment())
                .collect(Collectors.joining("\n"));
        return "%sTitle: %s\nMedium: %s%s\nScene: %s\nDirector's markers:\n%s".formatted(
                scene.production() == null ? "" : "Production: " + scene.production() + "\n", scene.title(), scene.medium().label(),
                scene.genre() == null ? "" : ", " + scene.genre(), scene.description(), markers.isEmpty() ? "- (none)" : markers);
    }

    static String bullets(List<String> lines) {
        return lines.isEmpty() ? "- (none)" : lines.stream().map(line -> "- " + line).collect(Collectors.joining("\n"));
    }

}
