package com.thomasvitale.mousike.guardrail;

import java.time.Duration;

/**
 * What the guardrail decided about a request, with the probabilities behind it.
 */
public record GuardrailVerdict(Outcome outcome, double onTopic, double injection, double creativeWork, String model, Duration duration) {

    public boolean allowed() {
        return outcome == Outcome.ALLOWED;
    }

    public String reason() {
        return outcome.reason;
    }

    /** Why a request passes or stops, and what the composer reads when it stops. */
    public enum Outcome {

        ALLOWED("On topic", ""),

        INJECTION("Looked like an attempt to change the assistant's instructions", """
                I can only help with music: scenes to score, moods, instruments, progressions, orchestration, \
                and what's in your notebook. Ask me about one of those."""),

        CREATIVE_WORK("Asked for creative work, which stays with the composer", """
                That part is yours. Melodies, lyrics, orchestration: creating them is what makes us human, so I leave \
                them to you. I can bring back your notes, find sounds in your library and play the chords you choose."""),

        OFF_TOPIC("Not about music or your notes", """
                I can only help with music: scenes to score, moods, instruments, progressions, orchestration, \
                and what's in your notebook. Ask me about one of those.""");

        private final String reason;
        private final String answer;

        Outcome(String reason, String answer) {
            this.reason = reason;
            this.answer = answer;
        }

        String answer() {
            return answer;
        }

    }

}
