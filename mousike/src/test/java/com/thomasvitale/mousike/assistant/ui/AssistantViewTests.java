package com.thomasvitale.mousike.assistant.ui;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import com.thomasvitale.mousike.assistant.Answer;
import com.thomasvitale.mousike.assistant.StudioAssistant;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.markdown.Markdown;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "mousike.demo.seed=false")
@ViewPackages(packages = "com.thomasvitale.mousike")
class AssistantViewTests extends SpringBrowserlessTest {

    private static final String STARTER = "Find me a solo cello in my library.";
    private static final String NEW_CONVERSATION = "New conversation";

    @MockitoBean
    private StudioAssistant assistant;

    @Test
    void answerAppearsAndConversationStartsOver() {
        when(assistant.reply(any(), any(), any())).thenAnswer(call -> {
            call.<Consumer<String>>getArgument(2).accept("Looking in your library");
            return new Answer("Try the solo cello.", List.of(), List.of(), null, "test-model", Duration.ofMillis(5));
        });
        navigate(AssistantView.class);
        assertThat(find(Button.class).withText(NEW_CONVERSATION).exists()).isFalse();

        test(find(Button.class).withText(STARTER).single()).click();
        awaitAnswer();

        assertThat(test(find(Markdown.class).single()).getContent()).isEqualTo("Try the solo cello.");
        assertThat(find(Button.class).withText(STARTER).exists()).isFalse();

        test(find(Button.class).withText(NEW_CONVERSATION).single()).click();

        assertThat(find(Markdown.class).exists()).isFalse();
        assertThat(find(Button.class).withText(STARTER).exists()).isTrue();
    }

    @Test
    void failureStaysInTheComposersWords() {
        when(assistant.reply(any(), any(), any())).thenThrow(new IllegalStateException("connection refused by model"));
        navigate(AssistantView.class);

        test(find(Button.class).withText(STARTER).single()).click();
        awaitAnswer();

        assertThat(test(find(Markdown.class).single()).getContent()).isEqualTo("That didn't go through. Try again.");
    }

    private void awaitAnswer() {
        await().atMost(Duration.ofSeconds(5)).pollInSameThread().until(() -> {
            runPendingSignalsTasks();
            return find(Markdown.class).exists() && find(Button.class).withText(NEW_CONVERSATION).exists();
        });
    }

}
