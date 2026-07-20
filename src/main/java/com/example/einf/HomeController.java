package com.example.einf;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Optional;

public class HomeController {

    @FXML
    private Label welcomeText;

    @FXML
    private FlowPane moduleGrid;

    @FXML
    private FlowPane deadlineGrid;

    @FXML
    private Label gpaLabel;

    @FXML
    private Label ectsLabel;

    @FXML
    private BarChart<String, Number> gradesChart;

    private ModulVerwaltung modulVerwaltung;
    private DeadlineManager deadlineManager;

    public void setModulVerwaltung(ModulVerwaltung modulVerwaltung) {
        this.modulVerwaltung = modulVerwaltung;
        refreshModuleGrid();
    }

    public void setDeadlineManager(DeadlineManager deadlineManager) {
        this.deadlineManager = deadlineManager;
    }

    private void refreshModuleGrid() {
        if (moduleGrid == null || modulVerwaltung == null) return;
        moduleGrid.getChildren().clear();

        for (Modul m : modulVerwaltung.getModule()) {
            VBox card = new VBox(5);
            card.setPadding(new Insets(10));
            card.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-background-color: white; -fx-background-radius: 5;");
            card.setPrefWidth(200);

            Label nameLabel = new Label(m.getName());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label ectsLabel = new Label(m.getEcts() + " ECTS");
            Label semLabel = new Label(m.getSemester() != null ? m.getSemester().name() : "Kein Semester");
            
            String leistungInfo = "Keine Leistung";
            if (m.getLeistung() instanceof Pruefungsleistung p) {
                leistungInfo = "Note: " + p.getErreichteNote() + (p.isBestanden() ? " (Bestanden)" : " (Nicht bestanden)");
            } else if (m.getLeistung() instanceof Studienleistung s) {
                leistungInfo = "Studienleistung: " + (s.isBestanden() ? "Bestanden" : "Nicht bestanden");
            }
            Label leistungLabel = new Label(leistungInfo);

            Button deleteBtn = new Button("Löschen");
            deleteBtn.setStyle("-fx-text-fill: red;");
            deleteBtn.setOnAction(e -> {
                modulVerwaltung.deleteModul(m.getId());
                refreshModuleGrid();
            });

            card.getChildren().addAll(nameLabel, ectsLabel, semLabel, leistungLabel, deleteBtn);
            moduleGrid.getChildren().add(card);
        }
    }

    @FXML
    public void addModuleButtonOnAction(ActionEvent actionEvent) {
        Dialog<Modul> dialog = new Dialog<>();
        dialog.setTitle("Neues Modul hinzufügen");
        dialog.setHeaderText("Bitte Moduldetails eingeben:");

        ButtonType saveButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Modulname");
        TextField ectsField = new TextField();
        ectsField.setPromptText("ECTS (z.B. 5)");
        ComboBox<Semester> semesterBox = new ComboBox<>();
        semesterBox.getItems().addAll(Semester.values());
        CheckBox benotetBox = new CheckBox("Ist benotet?");

        ComboBox<String> leistungTypeBox = new ComboBox<>();
        leistungTypeBox.getItems().addAll("Keine", "Prüfungsleistung", "Studienleistung");
        leistungTypeBox.setValue("Keine");

        TextField noteField = new TextField();
        noteField.setPromptText("Note (z.B. 1.3)");
        noteField.setDisable(true);

        CheckBox bestandenBox = new CheckBox("Bestanden");
        bestandenBox.setDisable(true);

        leistungTypeBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            noteField.setDisable(!newVal.equals("Prüfungsleistung"));
            bestandenBox.setDisable(!newVal.equals("Studienleistung"));
            
            // Keep benotetBox in sync if the user manually changes the dropdown
            if (newVal.equals("Prüfungsleistung") && !benotetBox.isSelected()) {
                benotetBox.setSelected(true);
            } else if (newVal.equals("Studienleistung") && benotetBox.isSelected()) {
                benotetBox.setSelected(false);
            }
        });

        benotetBox.selectedProperty().addListener((obs, oldVal, isBenotet) -> {
            if (isBenotet) {
                leistungTypeBox.setValue("Prüfungsleistung");
            } else {
                leistungTypeBox.setValue("Studienleistung");
            }
        });

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("ECTS:"), 0, 1);
        grid.add(ectsField, 1, 1);
        grid.add(new Label("Semester:"), 0, 2);
        grid.add(semesterBox, 1, 2);
        grid.add(benotetBox, 1, 3);
        grid.add(new Label("Leistungstyp:"), 0, 4);
        grid.add(leistungTypeBox, 1, 4);
        grid.add(new Label("Note:"), 0, 5);
        grid.add(noteField, 1, 5);
        grid.add(bestandenBox, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText();
                    int ects = Integer.parseInt(ectsField.getText());
                    boolean istBenotet = benotetBox.isSelected();
                    Semester semester = semesterBox.getValue();

                    Modul m = new Modul(name, ects, istBenotet, semester);

                    String lType = leistungTypeBox.getValue();
                    if (lType.equals("Prüfungsleistung")) {
                        double note = Double.parseDouble(noteField.getText());
                        m.setLeistung(new Pruefungsleistung(m, note));
                    } else if (lType.equals("Studienleistung")) {
                        m.setLeistung(new Studienleistung(m, bestandenBox.isSelected()));
                    }

                    return m;
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Eingabefehler");
                    alert.setHeaderText("Fehlerhafte Eingabe");
                    alert.setContentText("Bitte prüfen Sie Ihre Eingaben (z.B. Zahlen bei ECTS und Note).");
                    alert.showAndWait();
                }
            }
            return null;
        });

        Optional<Modul> result = dialog.showAndWait();
        result.ifPresent(m -> {
            if (modulVerwaltung != null) {
                modulVerwaltung.addModul(m);
                refreshModuleGrid();
            }
        });
    }

    @FXML
    public void removeModuleButtonOnAction(ActionEvent actionEvent) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Info");
        info.setHeaderText(null);
        info.setContentText("Bitte nutzen Sie den Löschen-Button direkt auf der Modulkarte.");
        info.showAndWait();
    }

    @FXML
    public void onDeadlinesTabSelected(Event event) {
        if (deadlineManager != null) {
            refreshDeadlineGrid();
        }
    }

    private void refreshDeadlineGrid() {
        if (deadlineGrid == null || deadlineManager == null) return;
        deadlineGrid.getChildren().clear();

        for (Deadline d : deadlineManager.getAnstehendeDeadlines()) {
            addDeadlineCard(d);
        }
        for (Deadline d : deadlineManager.getUeberfaelligeDeadlines()) {
            addDeadlineCard(d);
        }
        // Could also show erledigt if needed, but getAnstehende filters them out.
    }

    private void addDeadlineCard(Deadline d) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setPrefWidth(200);

        String borderColor = "lightgray";
        if (d.getStatus() == DeadlineStatus.UEBERFAELLIG) borderColor = "red";
        else if (d.getStatus() == DeadlineStatus.ERLEDIGT) borderColor = "green";
        else if (d.getStatus() == DeadlineStatus.OFFEN) borderColor = "orange";

        card.setStyle("-fx-border-color: " + borderColor + "; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-color: white; -fx-background-radius: 5;");

        Label nameLabel = new Label(d.getTitel());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label dateLabel = new Label("Datum: " + d.getDatum().toString());
        Label statusLabel = new Label("Status: " + d.getStatus().name());

        Button doneBtn = new Button("Mark as Done");
        doneBtn.setOnAction(e -> {
            deadlineManager.markAsErledigt(d);
            refreshDeadlineGrid();
        });
        doneBtn.setDisable(d.istErledigt());

        card.getChildren().addAll(nameLabel, dateLabel, statusLabel, doneBtn);
        deadlineGrid.getChildren().add(card);
    }

    @FXML
    public void addDeadlineButtonOnAction(ActionEvent actionEvent) {
        Dialog<Deadline> dialog = new Dialog<>();
        dialog.setTitle("Neue Deadline hinzufügen");
        dialog.setHeaderText("Bitte Deadlinedetails eingeben:");

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
        if (DeadlineTyp.values().length > 0) typBox.setValue(DeadlineTyp.values()[0]);

        TextField modulNameField = new TextField();
        modulNameField.setPromptText("Modulname (optional)");

        grid.add(new Label("Beschreibung:"), 0, 0);
        grid.add(beschreibungField, 1, 0);
        grid.add(new Label("Datum:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Typ:"), 0, 2);
        grid.add(typBox, 1, 2);
        grid.add(new Label("Modulname:"), 0, 3);
        grid.add(modulNameField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Deadline(beschreibungField.getText(), datePicker.getValue(), typBox.getValue(), modulNameField.getText());
            }
            return null;
        });

        Optional<Deadline> result = dialog.showAndWait();
        result.ifPresent(d -> {
            if (deadlineManager != null) {
                deadlineManager.addDeadline(d);
                refreshDeadlineGrid();
            }
        });
    }

    @FXML
    public void onGradesTabSelected(Event event) {
        refreshGradesTab();
    }

    private void refreshGradesTab() {
        if (modulVerwaltung == null) return;

        LeistungsRechner rechner = new LeistungsRechner(modulVerwaltung.getModule());
        
        double gpa = rechner.berechneNotendurchschnitt();
        gpaLabel.setText(String.format("%.2f", gpa));

        int passedEcts = rechner.berechneBestandeneEcts();
        int totalEcts = rechner.berechneGesamtEcts();
        ectsLabel.setText(passedEcts + " / " + totalEcts);

        if (gradesChart != null) {
            gradesChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Noten pro Modul");

            for (Modul m : modulVerwaltung.getModule()) {
                if (m.istBenotet() && m.getLeistung() instanceof Pruefungsleistung p) {
                    series.getData().add(new XYChart.Data<>(m.getName(), p.getErreichteNote()));
                }
            }
            gradesChart.getData().add(series);
        }
    }
}