package com.thomasvitale.mousike.notebook;

import com.thomasvitale.mousike.notebook.internal.ClassificationPolicy;
import com.thomasvitale.mousike.notebook.internal.NoteTagger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An evaluation of the decision model, written as a plain JUnit test: every demo note must be classified as the kind
 * it is written for, confidently enough that nobody has to review it, and the two notes typed live in the demo must
 * behave as the script expects. It calls the real model, so it is not part of {@code ./gradlew test}; run it with
 * {@code ./gradlew calibrationTest} (Ollama running) after changing the notes, the kinds, the thresholds or the model.
 */
@Tag("calibration")
@SpringBootTest(properties = "mousike.demo.seed=false")
class NoteClassificationCalibrationTests {

    @Autowired
    NoteTagger noteTagger;

    @Autowired
    ClassificationPolicy classificationPolicy;

    static Stream<Arguments> demoNotes() throws IOException {
        try (InputStream in = new ClassPathResource("demo/notes.json").getInputStream()) {
            List<DemoNote> notes = JsonMapper.builder().build().readValue(in, new TypeReference<List<DemoNote>>() { });
            return notes.stream().map(note -> Arguments.of(note.kind(), note.content()));
        }
    }

    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("demoNotes")
    void demoNoteIsClassifiedConfidentlyAsWritten(NoteKind expected, String content) {
        NoteTags tags = noteTagger.tag(content);

        assertThat(tags.kind()).as(ranking(tags)).isEqualTo(expected);
        assertThat(classificationPolicy.needsReview(tags)).as(ranking(tags)).isFalse();
    }

    @Test
    void noteTypedLiveIsClassifiedConfidently() {
        NoteTags tags = noteTagger.tag("The Irish tin whistle suits cheerful fairytale themes, in D or G with cuts and rolls.");

        assertThat(tags.kind()).as(ranking(tags)).isEqualTo(NoteKind.INSTRUMENT);
        assertThat(classificationPolicy.needsReview(tags)).as(ranking(tags)).isFalse();
    }

    @Test
    void ambiguousNoteTypedLiveNeedsReview() {
        NoteTags tags = noteTagger.tag("Muted trumpet for noir.");

        assertThat(classificationPolicy.needsReview(tags)).as(ranking(tags)).isTrue();
    }

    /** The model's top three guesses, so a failure says what it thought instead. */
    private static String ranking(NoteTags tags) {
        return tags.ranked().stream().limit(3).map(KindGuess::describe).collect(Collectors.joining(", "));
    }

    record DemoNote(NoteKind kind, String content) {
    }

}
