package com.thomasvitale.mousike.scenes.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SceneExtractorTests {

    @Test
    void markerCommentsStartWithACapitalLetter() {
        assertThat(SceneExtractor.capitalise("uneasy, high-pitched music that starts rising"))
                .isEqualTo("Uneasy, high-pitched music that starts rising");
    }

    @Test
    void restOfTheCommentIsLeftAsSpoken() {
        assertThat(SceneExtractor.capitalise("stay in D minor, then the brass")).isEqualTo("Stay in D minor, then the brass");
        assertThat(SceneExtractor.capitalise("Already capitalised")).isEqualTo("Already capitalised");
        assertThat(SceneExtractor.capitalise("ébauche d'un thème")).isEqualTo("Ébauche d'un thème");
        assertThat(SceneExtractor.capitalise("")).isEmpty();
    }

    @Test
    void timesAreNormalisedToMinutesAndSeconds() {
        assertThat(SceneExtractor.normalise("75")).isEqualTo("01:15");
        assertThat(SceneExtractor.normalise("1:5")).isEqualTo("01:05");
        assertThat(SceneExtractor.normalise("00:15")).isEqualTo("00:15");
    }

}
