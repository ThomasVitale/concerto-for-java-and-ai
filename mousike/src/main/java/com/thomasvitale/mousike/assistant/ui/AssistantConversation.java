package com.thomasvitale.mousike.assistant.ui;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.thomasvitale.mousike.assistant.Answer;
import com.thomasvitale.mousike.assistant.StudioAssistant;

import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * The conversation on screen, for as long as the browser session lasts: it survives leaving the view, even while the
 * assistant is still working. The model's memory of it lives in the session repository, under {@link #sessionId}.
 * Signals can be written from any thread, so the assistant updates them directly from its own.
 */
@Component
@VaadinSessionScope
class AssistantConversation {

    private static final Logger log = LoggerFactory.getLogger(AssistantConversation.class);

    /** One message and what came of it: the tool calls on the way and the answer, or none if it failed. */
    record Exchange(String question, List<String> steps, @Nullable Answer answer) {
    }

    /** The message being answered right now. */
    record Pending(String question, List<String> steps) {

        Pending withStep(String step) {
            return new Pending(question, Stream.concat(steps.stream(), Stream.of(step)).toList());
        }

    }

    private final StudioAssistant assistant;
    private final AsyncTaskExecutor executor;
    private final ListSignal<Exchange> exchanges = new ListSignal<>();
    private final ValueSignal<@Nullable Pending> pending = new ValueSignal<>((Pending) null);
    private final Signal<Boolean> working = pending.map(Objects::nonNull);
    private final Signal<Boolean> started = Signal.computed(() -> working.get() || !exchanges.get().isEmpty());
    private final Signal<Boolean> answered = Signal.computed(() -> started.get() && !working.get());
    private String sessionId = UUID.randomUUID().toString();

    AssistantConversation(StudioAssistant assistant, @Qualifier("applicationTaskExecutor") AsyncTaskExecutor executor) {
        this.assistant = assistant;
        this.executor = executor;
    }

    ListSignal<Exchange> exchanges() {
        return exchanges;
    }

    Signal<@Nullable Pending> pending() {
        return pending;
    }

    /** A message is being answered. */
    Signal<Boolean> working() {
        return working;
    }

    /** Something has been asked since the last reset. */
    Signal<Boolean> started() {
        return started;
    }

    /** There are answers, and none is on its way. */
    Signal<Boolean> answered() {
        return answered;
    }

    void send(String question) {
        String id = sessionId;
        pending.set(new Pending(question, List.of()));
        CompletableFuture.supplyAsync(() -> assistant.reply(id, question, this::addStep), executor)
                .whenComplete((answer, failure) -> {
                    if (failure != null) {
                        log.error("Assistant failed", failure);
                    }
                    finish(question, answer);
                });
    }

    /** Starts over: a new session for the model, an empty page for the composer. */
    void reset() {
        sessionId = UUID.randomUUID().toString();
        exchanges.clear();
    }

    private void addStep(String step) {
        pending.update(current -> current == null ? null : current.withStep(step));
    }

    private void finish(String question, @Nullable Answer answer) {
        Pending current = pending.peek();
        exchanges.insertLast(new Exchange(question, current == null ? List.of() : current.steps(), answer));
        pending.set(null);
    }

}
