package com.thomasvitale.mousike.scenes.ui.internal;

import java.util.List;

import com.thomasvitale.mousike.scenes.Scene;
import com.thomasvitale.mousike.scenes.SceneService;
import com.thomasvitale.mousike.scenes.ui.SceneSheet;
import com.thomasvitale.mousike.ui.ExtendedMode;
import com.thomasvitale.mousike.ui.ViewHeader;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;

/**
 * Scenes: the scenes you discussed with a director, marker by marker.
 */
@Route("scenes")
@PageTitle("Scenes")
@Menu(title = "Scenes", icon = "vaadin:film", order = 2)
public class ScenesView extends Div {

    private final SceneService sceneService;
    private final ExtendedMode extendedMode;
    private final AsyncTaskExecutor executor;
    private final Div sheets = new Div();

    public ScenesView(SceneService sceneService, ExtendedMode extendedMode,
            @Qualifier("applicationTaskExecutor") AsyncTaskExecutor executor) {
        this.sceneService = sceneService;
        this.extendedMode = extendedMode;
        this.executor = executor;
        addClassName("studio-view");
        addClassName("scenes-view");

        // Extended mode: a scene starts from the director's recording; plain mode: everything typed in by hand
        Button newScene = new Button("New scene", e -> openNewScene());
        newScene.addThemeVariants(ButtonVariant.PRIMARY);
        Signal.effect(newScene, () -> newScene.setIcon(extendedMode.enabled().get()
                ? VaadinIcon.MICROPHONE.create() : VaadinIcon.PLUS.create()));
        ViewHeader header = new ViewHeader("Your scenes", "")
                .bindPurpose(extendedMode.enabled().map(on -> on
                        ? "Record what the director wants at each moment of a scene."
                        : "Write down what the director wants at each moment of a scene."))
                .withActions(newScene);

        sheets.addClassName("scene-sheets");
        add(header, sheets);
        refresh();
    }

    private void refresh() {
        sheets.removeAll();
        List<Scene> scenes = sceneService.list();
        if (scenes.isEmpty()) {
            Paragraph empty = new Paragraph("No scenes yet. Start one from a recording or from typed notes.");
            empty.addClassName("empty-state");
            sheets.add(empty);
            return;
        }
        scenes.forEach(scene -> sheets.add(new SceneSheet(scene, this::confirmDelete)));
    }

    private void openNewScene() {
        new NewSceneDialog(sceneService, executor, extendedMode.enabled().peek(), scene -> {
            refresh();
            Notification toast = Notification.show("Saved \"%s\" with %d markers".formatted(scene.title(), scene.markers().size()),
                    4000, Notification.Position.BOTTOM_START);
            toast.addThemeVariants(NotificationVariant.SUCCESS);
        }).open();
    }

    private void confirmDelete(Scene scene) {
        ConfirmDialog dialog = new ConfirmDialog("Delete this scene?", scene.title(), "Delete", e -> {
            sceneService.delete(scene.requiredId());
            refresh();
        }, "Cancel", e -> { });
        dialog.setConfirmButtonTheme("error primary");
        dialog.open();
    }

}
