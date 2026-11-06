package com.thomasvitale.mousike.ui;

import com.thomasvitale.mousike.assistant.ui.AssistantView;
import com.thomasvitale.mousike.notebook.ui.NotebookView;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "mousike.demo.seed=false")
@ViewPackages(packages = "com.thomasvitale.mousike")
class ExtendedOnlyTests extends SpringBrowserlessTest {

    @Autowired
    private ObjectProvider<ExtendedMode> extendedMode;

    @Test
    void extendedViewOpensWhileExtended() {
        assertThat(navigate(AssistantView.class)).isInstanceOf(AssistantView.class);
    }

    @Test
    void extendedViewForwardsToTheNotebookWhileOff() {
        extendedMode.getObject().enabled().set(false);

        navigate("assistant", NotebookView.class);

        assertThat(getCurrentView()).isInstanceOf(NotebookView.class);
    }

    @Test
    void switchingOffLeavesAnExtendedView() {
        navigate(AssistantView.class);

        extendedMode.getObject().enabled().set(false);
        runPendingSignalsTasks();

        assertThat(getCurrentView()).isInstanceOf(NotebookView.class);
    }

}
