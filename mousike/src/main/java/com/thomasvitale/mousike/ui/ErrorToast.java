package com.thomasvitale.mousike.ui;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/**
 * Tells the composer something failed, with the root cause rather than the exception wrapped around it.
 */
public final class ErrorToast {

    private ErrorToast() {
    }

    public static void show(String message) {
        Notification toast = Notification.show(message, 8000, Notification.Position.BOTTOM_CENTER);
        toast.addThemeVariants(NotificationVariant.ERROR);
    }

    public static void show(String context, Throwable failure) {
        show(context + ": " + rootMessage(failure));
    }

    private static String rootMessage(Throwable failure) {
        Throwable root = failure;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
    }

}
