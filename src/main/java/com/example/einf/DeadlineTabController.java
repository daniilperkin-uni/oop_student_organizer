package com.example.einf;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Owns everything specific to the "Fristen" (deadlines) tab: the card grid,
 * the add/edit deadline dialog and the sort-direction toggle.
 *
 * <p>
 * Extracted from the former 674-line HomeController; wired programmatically
 * by HomeController and persists every mutation through the shared save
 * callback.
 * </p>
 */
class DeadlineTabController {

    private FlowPane deadlineGrid;
    private Button deadlineSortDirectionButton;

    private DeadlineManager deadlineManager;
    private Runnable onSave;

    private boolean deadlineSortDescending = false;

    void setDeadlineGrid(FlowPane deadlineGrid) {
        this.deadlineGrid = deadlineGrid;
    }

    void setDeadlineSortDirectionButton(Button deadlineSortDirectionButton) {
        this.deadlineSortDirectionButton = deadlineSortDirectionButton;
    }

    void setDeadlineManager(DeadlineManager deadlineManager) {
        this.deadlineManager = deadlineManager;
    }

    void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    void initializeFilters() {
        if (deadlineSortDirectionButton != null) {
            deadlineSortDirectionButton.setOnAction(e -> {
                deadlineSortDescending = !deadlineSortDescending;
                deadlineSortDirectionButton.setText(deadlineSortDescending ? "▼" : "▲");
                refreshDeadlineGrid();
            });
        }
    }

    void refreshDeadlineGrid() {
        if (deadlineGrid == null || deadlineManager == null) return;
        deadlineGrid.getChildren().clear();

        List<Deadline> deadlines = deadlineManager.getAllDeadlinesSortiert();
        if (deadlineSortDescending) {
            Collections.reverse(deadlines);
        }
        for (Deadline d : deadlines) {
            deadlineGrid.getChildren().add(createDeadlineCard(d));
        }
    }

    void onTabSelected() {
        if (deadlineManager != null) {
            refreshDeadlineGrid();
        }
    }

    void addDeadlineButtonOnAction(ActionEvent actionEvent) {
        showDeadlineDialog(null).ifPresent(d -> {
            deadlineManager.addDeadline(d);
            refreshDeadlineGrid();
            saveChanges();
        });
    }

    private void saveChanges() {
        if (onSave != null) {
            onSave.run();
        }
    }

    private VBox createDeadlineCard(Deadline d) {
        VBox card = new VBox(5);
        card.getStyleClass().addAll("card", statusStyleClass(d.getStatus()));

        Label nameLabel = new Label(d.getTitel());
        nameLabel.getStyleClass().add("card-title");

        Label dateLabel = new Label("Datum: " + d.getDatum());
        dateLabel.getStyleClass().add("card-subtitle");

        Label typLabel = new Label("Typ: " + formatDeadlineTyp(d.getTyp()));
        typLabel.getStyleClass().add("card-subtitle");

        String modulText = d.getModulName() != null && !d.getModulName().isBlank()
                ? d.getModulName()
                : "Kein Modul verknüpft";
        Label modulLabel = new Label("Modul: " + modulText);
        modulLabel.getStyleClass().add("card-subtitle");

        Label statusLabel = new Label("Status: " + formatDeadlineStatus(d.getStatus()));
        statusLabel.getStyleClass().add("card-subtitle");

        HBox buttonBar = new HBox(8);
        buttonBar.getStyleClass().add("button-bar");

        Button editBtn = new Button("Bearbeiten");
        editBtn.setOnAction(e -> bearbeiteDeadline(d));

        Button doneBtn = new Button("Erledigt");
        doneBtn.getStyleClass().add("btn-done");
        doneBtn.setOnAction(e -> {
            deadlineManager.markAsErledigt(d);
            refreshDeadlineGrid();
            saveChanges();
        });
        doneBtn.setDisable(d.istErledigt());

        Button deleteBtn = new Button("Löschen");
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setOnAction(e -> {
            if (UiDialogs.bestaetigeLoeschen("Frist löschen", "Frist wirklich löschen?", d.getTitel())) {
                deadlineManager.deleteDeadline(d);
                refreshDeadlineGrid();
                saveChanges();
            }
        });

        buttonBar.getChildren().addAll(editBtn, doneBtn, deleteBtn);
        card.getChildren().addAll(nameLabel, dateLabel, typLabel, modulLabel, statusLabel, buttonBar);
        return card;
    }

    private String statusStyleClass(DeadlineStatus status) {
        return switch (status) {
            case UEBERFAELLIG -> "status-ueberfaellig";
            case ERLEDIGT -> "status-erledigt";
            case OFFEN -> "status-offen";
        };
    }

    private String formatDeadlineStatus(DeadlineStatus status) {
        return switch (status) {
            case UEBERFAELLIG -> "Überfällig";
            case ERLEDIGT -> "Erledigt";
            case OFFEN -> "Offen";
        };
    }

    private String formatDeadlineTyp(DeadlineTyp typ) {
        if (typ == null) return "Unbekannt";
        return switch (typ) {
            case ANMELDUNG -> "Anmeldung";
            case ABGABE -> "Abgabe";
            case KLAUSUR -> "Klausur";
        };
    }

    private void bearbeiteDeadline(Deadline existing) {
        showDeadlineDialog(existing).ifPresent(updated -> {
            existing.setBeschreibung(updated.getBeschreibung());
            existing.setDatum(updated.getDatum());
            existing.setTyp(updated.getTyp());
            existing.setModulName(updated.getModulName());
            refreshDeadlineGrid();
            saveChanges();
        });
    }

    private Optional<Deadline> showDeadlineDialog(Deadline existing) {
        boolean isEdit = existing != null;
        Dialog<Deadline> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Frist bearbeiten" : "Neue Frist hinzufügen");
        dialog.setHeaderText("Bitte Fristdetails eingeben:");

        ButtonType saveButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField beschreibungField = new TextField();
        beschreibungField.setPromptText("Titel / Beschreibung");

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());

        ComboBox<DeadlineTyp> typBox = new ComboBox<>();
        typBox.getItems().addAll(DeadlineTyp.values());
        typBox.setValue(DeadlineTyp.ANMELDUNG);

        TextField modulNameField = new TextField();
        modulNameField.setPromptText("Modulname (optional)");

        if (isEdit) {
            beschreibungField.setText(existing.getBeschreibung());
            datePicker.setValue(existing.getDatum());
            typBox.setValue(existing.getTyp());
            modulNameField.setText(existing.getModulName());
        }

        grid.add(new Label("Beschreibung:"), 0, 0);
        grid.add(beschreibungField, 1, 0);
        grid.add(new Label("Datum:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Typ:"), 0, 2);
        grid.add(typBox, 1, 2);
        grid.add(new Label("Modulname:"), 0, 3);
        grid.add(modulNameField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            Optional<String> beschreibungError = EingabeValidierung.validiereDeadlineBeschreibung(beschreibungField.getText());
            if (beschreibungError.isPresent()) {
                UiDialogs.zeigeFehler("Eingabefehler", "Fehlerhafte Eingabe", beschreibungError.get());
                event.consume();
                return;
            }
            Optional<String> datumError = EingabeValidierung.validiereDatum(datePicker.getValue());
            if (datumError.isPresent()) {
                UiDialogs.zeigeFehler("Eingabefehler", "Fehlerhafte Eingabe", datumError.get());
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton != saveButtonType) return null;

            return new Deadline(
                    beschreibungField.getText().trim(),
                    datePicker.getValue(),
                    typBox.getValue(),
                    modulNameField.getText().trim());
        });

        return dialog.showAndWait();
    }
}
