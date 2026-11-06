package com.thomasvitale.mousike.compositionplan.ui;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.thomasvitale.mousike.compositionplan.CompositionPlan;
import com.thomasvitale.mousike.compositionplan.CompositionPlanner;
import com.thomasvitale.mousike.library.InstrumentLibrary;
import com.thomasvitale.mousike.library.LibraryStatus;
import com.thomasvitale.mousike.chords.MidiOutput;
import com.thomasvitale.mousike.chords.ChordProgressionService;
import com.thomasvitale.mousike.scenes.Scene;
import com.thomasvitale.mousike.scenes.SceneService;
import com.thomasvitale.mousike.scenes.ui.SceneSheet;
import com.thomasvitale.mousike.ui.ExtendedOnly;
import com.thomasvitale.mousike.ui.ViewHeader;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;

/**
 * Pick a scene, watch the planner read your notes and your library, get a composition plan you can hear.
 */
@ExtendedOnly
@Route("composition-plan")
@PageTitle("Composition Plan")
@Menu(title = "Composition Plan", icon = "vaadin:clipboard-text", order = 4)
public class CompositionPlanView extends Div {

    private static final Logger log = LoggerFactory.getLogger(CompositionPlanView.class);

    /** The page is gone, so there is nothing to update. It has to be a handler: without one, accessLater throws. */
    private static final SerializableRunnable NOTHING_TO_SHOW = () -> { };

    private final CompositionPlanner planner;
    private final ChordProgressionService progressions;
    private final MidiOutput midiOutput;
    private final AsyncTaskExecutor executor;

    private final ComboBox<Scene> scenePicker = new ComboBox<>("Scene");
    private final Button plan = new Button("Plan the composition", VaadinIcon.CLIPBOARD_TEXT.create());
    private final Span libraryStatus = new Span();
    private final Div sceneArea = new Div();
    private final Div progress = new Div();
    private final Div sheet = new Div();

    public CompositionPlanView(CompositionPlanner planner, SceneService sceneService, ChordProgressionService progressions, MidiOutput midiOutput,
            InstrumentLibrary instrumentLibrary, @Qualifier("applicationTaskExecutor") AsyncTaskExecutor executor) {
        this.planner = planner;
        this.progressions = progressions;
        this.midiOutput = midiOutput;
        this.executor = executor;
        addClassName("studio-view");
        addClassName("composition-plan-view");

        plan.addThemeVariants(ButtonVariant.PRIMARY);
        plan.setEnabled(false);
        plan.addClickListener(e -> planSelected());
        ViewHeader header = new ViewHeader("Plan the composition",
                "Pick a scene. It reads your notes and your instrument library, then writes a plan for your composition, with ideas you can hear.")
                .withActions(plan);

        List<Scene> scenes = sceneService.list();
        scenePicker.setItems(scenes);
        scenePicker.setItemLabelGenerator(scene -> scene.production() == null ? scene.title() : scene.production() + " · " + scene.title());
        scenePicker.setPlaceholder(scenes.isEmpty() ? "No scenes yet: add one in Scenes first" : "Choose a scene");
        scenePicker.setEnabled(!scenes.isEmpty());
        scenePicker.addValueChangeListener(e -> showScene(e.getValue()));
        scenePicker.addClassName("composition-scene-picker");

        libraryStatus.addClassName("composition-library-status");
        Div controls = new Div(scenePicker, libraryStatus);
        controls.addClassName("composition-controls");

        sceneArea.addClassName("composition-scene");
        progress.addClassName("composition-progress");
        sheet.addClassName("composition-sheet-area");
        add(header, controls, sceneArea, progress, sheet);

        UI ui = UI.getCurrent();
        CompletableFuture.supplyAsync(instrumentLibrary::status, executor).thenAccept(ui.accessLater(this::showLibraryStatus, NOTHING_TO_SHOW));
    }

    private void showLibraryStatus(LibraryStatus status) {
        libraryStatus.removeAll();
        Span dot = new Span();
        dot.addClassName(status.online() ? "model-badge-dot-local" : "model-badge-dot-offline");
        libraryStatus.add(dot, new Span(status.online()
                ? "Instrument library connected"
                : "Instrument library offline: the plan will name instruments, not presets"));
    }

    private void showScene(Scene scene) {
        sceneArea.removeAll();
        progress.removeAll();
        sheet.removeAll();
        plan.setEnabled(scene != null);
        if (scene == null) {
            return;
        }
        sceneArea.add(new SceneSheet(scene));
        planner.latestFor(scene.requiredId()).ifPresent(existing -> {
            sheet.add(new CompositionSheetView(existing, progressions, midiOutput, executor));
            plan.setText("Plan it again");
        });
    }

    private void planSelected() {
        Scene scene = scenePicker.getValue();
        if (scene == null) {
            return;
        }
        plan.setEnabled(false);
        scenePicker.setEnabled(false);
        progress.removeAll();
        sheet.removeAll();
        // A page reload mid-plan must not abort it: the finished plan waits under its scene
        UI ui = UI.getCurrent();
        SerializableConsumer<String> onStep = ui.accessLater(this::addStep, NOTHING_TO_SHOW);
        CompletableFuture.supplyAsync(() -> planner.plan(scene.requiredId(), onStep), executor)
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        log.error("Composition planning failed", failure);
                    }
                    ui.accessLater(() -> showOutcome(result), NOTHING_TO_SHOW).run();
                });
    }

    private void showOutcome(@Nullable CompositionPlan result) {
        plan.setEnabled(true);
        scenePicker.setEnabled(true);
        if (result == null) {
            Paragraph error = new Paragraph("The plan didn't come together. Try again.");
            error.addClassName("refused");
            sheet.add(error);
            return;
        }
        sheet.add(new CompositionSheetView(result, progressions, midiOutput, executor));
        plan.setText("Plan it again");
    }

    private void addStep(String step) {
        progress.getChildren().forEach(child -> child.getElement().getClassList().remove("current"));
        Span line = new Span(step);
        line.addClassNames("composition-progress-step", "current");
        progress.add(line);
    }
}
