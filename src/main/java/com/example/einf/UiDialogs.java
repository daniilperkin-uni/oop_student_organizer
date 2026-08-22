package com.example.einf;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Shared helpers for the confirmation/error dialogs used across the tab
 * controllers. Extracted from HomeController so every tab shows identical
 * dialogs without copy-pasting Alert construction.
 */
final class UiDialogs {

    private UiDialogs() {
    }

    /**
     * Shows a modal error dialog.
     *
     * @param titel  window title of the dialog
     * @param header headline shown above the content
     * @param inhalt detailed message body
     */
    static void zeigeFehler(String titel, String header, String inhalt) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titel);
        alert.setHeaderText(header);
        alert.setContentText(inhalt);
        alert.showAndWait();
    }

    /**
     * Shows a modal delete confirmation and reports the user's choice.
     *
     * @param titel  window title of the dialog
     * @param header headline shown above the content
     * @param name   name of the entity about to be removed
     * @return true only when the user acknowledged with OK
     */
    static boolean bestaetigeLoeschen(String titel, String header, String name) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titel);
        alert.setHeaderText(header);
        alert.setContentText("Möchten Sie \"" + name + "\" endgültig entfernen?");
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }
}
