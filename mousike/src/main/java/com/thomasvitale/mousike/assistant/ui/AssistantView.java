package com.thomasvitale.mousike.assistant.ui;

import java.util.List;
import java.util.Locale;

import com.thomasvitale.mousike.assistant.Answer;
import com.thomasvitale.mousike.assistant.Citation;
import com.thomasvitale.mousike.assistant.ui.AssistantConversation.Exchange;
import com.thomasvitale.mousike.assistant.ui.AssistantConversation.Pending;
import com.thomasvitale.mousike.chords.ChordProgression;
import com.thomasvitale.mousike.chords.MidiOutput;
import com.thomasvitale.mousike.chords.ui.ChordProgressionCard;
import com.thomasvitale.mousike.guardrail.GuardrailVerdict;
import com.thomasvitale.mousike.ui.ExtendedOnly;
import com.thomasvitale.mousike.ui.ModelBadge;
import com.thomasvitale.mousike.ui.ViewHeader;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInputVariant;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;

/**
 * A conversation with the studio's assistant. Each answer stays in place with what the assistant did to reach it, the
 * chord progressions it proposed and the notes it read.
 */
@Route("assistant")
@PageTitle("Assistant")
@Menu(title = "Assistant", icon = "vaadin:comment-o", order = 3)
@ExtendedOnly
public class AssistantView extends Div {

    private static final List<String> STARTERS = List.of(
            "I'm scoring an action scene. Give me ideas from my notes.",
            "Find me a solo cello in my library.",
            "Let me hear I-V-IV-vi in C major.");

    private final AssistantConversation conversation;
    private final MidiOutput midiOutput;
    private final AsyncTaskExecutor executor;

    public AssistantView(AssistantConversation conversation, MidiOutput midiOutput,
            @Qualifier("applicationTaskExecutor") AsyncTaskExecutor executor) {
        this.conversation = conversation;
        this.midiOutput = midiOutput;
        this.executor = executor;
        addClassName("studio-view");
        addClassName("assistant-view");
        // Empty: one big input at the top of the page. From the first message on: a chat panel with the input at its foot
        bindClassName("conversation-started", conversation.started());

        Button startOver = new Button("New conversation", VaadinIcon.PLUS.create(), e -> conversation.reset());
        startOver.addThemeVariants(ButtonVariant.PRIMARY);
        startOver.bindVisible(conversation.answered());

        ViewHeader header = new ViewHeader("Your assistant",
                "Ask about your notes, look for sounds in your library, or hear a chord progression.")
                .withActions(startOver);

        Div starters = new Div();
        starters.addClassName("starters");
        STARTERS.forEach(text -> {
            Button chip = new Button(text, e -> send(text));
            chip.addThemeVariants(ButtonVariant.SMALL);
            chip.addClassName("starter");
            starters.add(chip);
        });
        starters.bindVisible(Signal.not(conversation.started()));

        Div transcript = new Div();
        transcript.addClassName("transcript");
        transcript.bindChildren(conversation.exchanges(), exchange -> render(exchange.peek()));

        Div pendingQuestion = question("");
        Div pendingSteps = new Div();
        pendingSteps.addClassName("exchange-steps");
        pendingSteps.addClassName("working");
        Div pending = new Div(pendingQuestion, pendingSteps);
        pending.addClassName("exchange");
        pending.bindVisible(conversation.working());

        MessageInput input = new MessageInput();
        input.addClassName("assistant-input");
        input.addThemeVariants(MessageInputVariant.AURA_ICON_BUTTON);
        input.setWidthFull();
        input.addSubmitListener(e -> send(e.getValue()));
        input.bindEnabled(Signal.not(conversation.working()));

        Div end = new Div();
        Div scroll = new Div(starters, transcript, pending, end);
        scroll.addClassName("conversation-scroll");
        Div composer = new Div(input);
        composer.addClassName("conversation-composer");
        Div page = new Div(scroll, composer);
        page.addClassName("conversation-panel");
        add(header, page);

        // Whenever the input is free again: the latest answer in view, the cursor ready for the next message
        Signal.effect(this, () -> {
            if (conversation.working().get()) {
                return;
            }
            if (!conversation.exchanges().get().isEmpty()) {
                end.scrollIntoView();
            }
            input.focus();
        });
        Signal.effect(this, () -> {
            Pending current = conversation.pending().get();
            pendingSteps.removeAll();
            if (current != null) {
                pendingQuestion.setText(current.question());
                pendingSteps.add(new Span("Processing…"));
                current.steps().forEach(step -> pendingSteps.add(new Span(step)));
                end.scrollIntoView();
            }
        });
    }

    private void send(String text) {
        if (!text.isBlank()) {
            conversation.send(text.strip());
        }
    }

    private Div render(Exchange exchange) {
        Div block = new Div(question(exchange.question()));
        block.addClassName("exchange");
        if (!exchange.steps().isEmpty()) {
            Div steps = new Div();
            steps.addClassName("exchange-steps");
            exchange.steps().forEach(step -> steps.add(new Span(step)));
            block.add(steps);
        }
        Answer answer = exchange.answer();
        if (answer == null) {
            block.add(reply("That didn't go through. Try again.", true));
            return block;
        }
        block.add(reply(answer.markdown().isBlank() ? "(no answer)" : answer.markdown(), answer.refused()));
        for (ChordProgression progression : answer.chordProgressions()) {
            block.add(new ChordProgressionCard(progression, midiOutput, executor));
        }
        block.add(evidence(answer));
        return block;
    }

    private static Div question(String text) {
        Div question = new Div();
        question.setText(text);
        question.addClassName("exchange-question");
        return question;
    }

    private static Markdown reply(String markdown, boolean refused) {
        Markdown reply = new Markdown(markdown);
        reply.addClassName("exchange-answer");
        if (refused) {
            reply.addClassName("refused");
        }
        return reply;
    }

    private static Div evidence(Answer answer) {
        Div evidence = new Div();
        evidence.addClassName("evidence");
        GuardrailVerdict verdict = answer.verdict();
        if (answer.refused() && verdict != null) {
            evidence.add(verdict("Stopped by the guardrail: %s (on topic %.0f%%, hijack attempt %.0f%%, creative work %.0f%%)".formatted(
                    verdict.reason().toLowerCase(Locale.ROOT), verdict.onTopic() * 100, verdict.injection() * 100, verdict.creativeWork() * 100)),
                    new ModelBadge(verdict.model(), verdict.duration()));
            return evidence;
        }
        if (!answer.citations().isEmpty()) {
            Span title = new Span("Notes behind this answer");
            title.addClassName("evidence-title");
            evidence.add(title, citations(answer.citations()));
        }
        evidence.add(provenance(answer));
        return evidence;
    }

    private static Div citations(List<Citation> citations) {
        Div list = new Div();
        list.addClassName("evidence-list");
        for (Citation citation : citations) {
            Span kind = new Span(prettyKind(citation.kind()));
            kind.addClassName("evidence-kind");
            Span excerpt = new Span(citation.excerpt());
            excerpt.addClassName("evidence-excerpt");
            Div row = new Div(kind, excerpt);
            row.addClassName("evidence-row");
            row.getElement().setAttribute("data-kind", citation.kind().toLowerCase(Locale.ROOT));
            list.add(row);
        }
        return list;
    }

    private static Div provenance(Answer answer) {
        Div provenance = new Div(new ModelBadge(answer.model(), answer.duration()));
        GuardrailVerdict verdict = answer.verdict();
        if (verdict != null) {
            provenance.add(verdict("Guardrail passed: on topic %.0f%%, in %s".formatted(verdict.onTopic() * 100, ModelBadge.duration(verdict.duration()))));
        }
        provenance.addClassName("evidence-provenance");
        return provenance;
    }

    private static Span verdict(String text) {
        Span verdict = new Span(text);
        verdict.addClassName("evidence-verdict");
        return verdict;
    }

    private static String prettyKind(String kind) {
        String lower = kind.toLowerCase(Locale.ROOT).replace('_', ' ');
        return lower.isEmpty() ? "note" : Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

}
