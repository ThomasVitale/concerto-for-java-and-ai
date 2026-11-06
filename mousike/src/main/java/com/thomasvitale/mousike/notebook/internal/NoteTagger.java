package com.thomasvitale.mousike.notebook.internal;

import com.thomasvitale.mousike.notebook.KindGuess;
import com.thomasvitale.mousike.notebook.NoteKind;
import com.thomasvitale.mousike.notebook.NoteTags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.decision.DecisionModel;
import org.springframework.ai.decision.DecisionPrompt;
import org.springframework.ai.decision.DecisionResponse;
import org.springframework.ai.decision.answer.ChoiceAnswer;
import org.springframework.ai.decision.question.ChoiceQuestion;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class NoteTagger {

    private static final Logger logger = LoggerFactory.getLogger(NoteTagger.class);

    static final String KIND = "kind";

    private final DecisionModel decisionModel;
    private final ChoiceQuestion kindQuestion;

    public NoteTagger(DecisionModel decisionModel) {
        this.decisionModel = decisionModel;
        ChoiceQuestion.Builder kind = ChoiceQuestion.builder()
                .text("What kind of composition note is this?");
        for (NoteKind value : NoteKind.values()) {
            kind.option(value.name(), value.description());
        }
        this.kindQuestion = kind.build();
    }

    public NoteTags tag(String content) {
        DecisionPrompt prompt = DecisionPrompt.builder()
                .text(content)
                .question(KIND, kindQuestion)
                .build();

        long start = System.nanoTime();
        DecisionResponse response = decisionModel.call(prompt);
        Duration duration = Duration.ofNanos(System.nanoTime() - start);

        if (response.isRefused(KIND)) {
            throw new IllegalStateException("The decision model declined to classify this note");
        }
        ChoiceAnswer answer = response.getChoice(KIND);
        List<KindGuess> ranked = answer.getRankedOptions().stream()
                .map(option -> new KindGuess(NoteKind.valueOf(option), answer.getProbabilities().get(option)))
                .toList();
        logger.debug("Classified as {} (ranked {}, confidence {}) in {}", answer.getValue(), ranked,
                answer.getConfidence(), duration);

        String model = response.getMetadata().getModel();
        return new NoteTags(NoteKind.valueOf(answer.getValue()), ranked, model == null ? "decision model" : model, duration);
    }

}
