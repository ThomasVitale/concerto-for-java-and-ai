package com.thomasvitale.mousike.notebook.internal;

import com.thomasvitale.mousike.notebook.KindGuess;
import com.thomasvitale.mousike.notebook.NoteTags;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Decides when the decision model's choice stands on its own and when a person has to choose the kind.
 */
@Component
@EnableConfigurationProperties(ClassificationProperties.class)
public class ClassificationPolicy {

    private final ClassificationProperties classificationProperties;

    ClassificationPolicy(ClassificationProperties classificationProperties) {
        this.classificationProperties = classificationProperties;
    }

    public boolean needsReview(NoteTags tags) {
        List<KindGuess> ranked = tags.ranked();
        if (ranked.isEmpty()) {
            return false;
        }
        double top = ranked.getFirst().probability();
        double runnerUp = ranked.size() > 1 ? ranked.get(1).probability() : 0.0;
        return top < classificationProperties.minProbability() || top - runnerUp < classificationProperties.minMargin();
    }

}
