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

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Single FXML controller for the main view's three tabs (Module, Fristen,
 * Noten).
 *
 * <h2>Accepted SRP trade-off</h2>
 * Strictly, each tab should have its own controller (ModuleTabController,
 * DeadlineTabController, GradesTabController). This controller is ~640 lines
 * and handles three unrelated concerns, which violates the Single
 * Responsibility Principle.
 *
 * <p>The split is intentionally NOT performed here because the FXML
 * (home-view.fxml) binds a single {@code fx:controller="com.example.einf.HomeController"}
 * at the TabPane root, and every tab's controls and {@code onAction} handlers
 * resolve against that one controller. Splitting would require restructuring
 * the FXML (per-tab controllers via nested {@code <fx:include>} or separate
 * FXML files) and re-wiring all {@code fx:id} injections -- a larger
 * refactor with real regression risk on a JavaFX UI that has no automated
 * FXML-loading test coverage. The sections below are kept clearly separated
 * by comment banners to keep the three concerns navigable until the split is
 * done. The logic split points are:
 * <ul>
 *   <li>Module tab -- addModuleButtonOnAction, bearbeiteModul, showModulDialog,
 *       refreshModuleGrid, createModuleCard, filtering/sorting helpers</li>
 *   <li>Fristen tab -- addDeadlineButtonOnAction, bearbeiteDeadline,
 *       showDeadlineDialog, refreshDeadlineGrid, createDeadlineCard</li>
 *   <li>Noten tab -- onGradesTabSelected, refreshGradesTab</li>
 * </ul>
 */
public class HomeController {

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

    @FXML
    private TextField searchBar;

    @FXML
    private ComboBox<String> statusFilterBox;

    @FXML
    private ComboBox<String> semesterFilterBox;

    @FXML
    private ComboBox<String> sortFieldBox;

    @FXML
    private Button sortDirectionButton;

    @FXML
    private Button deadlineSortDirectionButton;

    private ModulVerwaltung modulVerwaltung;
    private DeadlineManager deadlineManager;
    private SpeicherManager speicherManager;
    private String searchQuery = "";
    private boolean moduleSortDescending = false;
    private boolean deadlineSortDescending = false;

    private static final String ALLE = "Alle";
    private static final String ALLE_SEMESTER = "Alle Semester";

    public void setModulVerwaltung(ModulVerwaltung modulVerwaltung) {
        this.modulVerwaltung = modulVerwaltung;
        refreshModuleGrid();
    }

    public void setDeadlineManager(DeadlineManager deadlineManager) {
        this.deadlineManager = deadlineManager;
    }

    public void setSpeicherManager(SpeicherManager speicherManager) {
        this.speicherManager = speicherManager;
    }

    public void initializeSearch() {
        if (searchBar != null) {
            searchBar.textProperty().addListener((obs, oldVal, newVal) -> {
                searchQuery = newVal == null ? "" : newVal.trim().toLowerCase(Locale.GERMAN);
                refreshModuleGrid();
            });
        }
    }

    @FXML
    public void clearSearchButtonOnAction(ActionEvent actionEvent) {
        if (searchBar != null) {
            searchBar.clear();
        }
    }

    public void initializeFilters() {
        if (statusFilterBox != null) {
            statusFilterBox.getItems().addAll(ALLE, "Bestanden", "Nicht bestanden");
            statusFilterBox.setValue(ALLE);
            statusFilterBox.valueProperty().addListener((obs, oldVal, newVal) -> refreshModuleGrid());
        }

        if (semesterFilterBox != null) {
            semesterFilterBox.getItems().add(ALLE_SEMESTER);
            for (Semester s : Semester.values()) {
                semesterFilterBox.getItems().add(s.getBezeichnung());
            }
            semesterFilterBox.setValue(ALLE_SEMESTER);
            semesterFilterBox.valueProperty().addListener((obs, oldVal, newVal) -> refreshModuleGrid());
        }

        if (sortFieldBox != null) {
            sortFieldBox.getItems().addAll("Name", "ECTS", "Note");
            sortFieldBox.setValue("Name");
            sortFieldBox.valueProperty().addListener((obs, oldVal, newVal) -> refreshModuleGrid());
        }

        if (sortDirectionButton != null) {
            sortDirectionButton.setOnAction(e -> {
                moduleSortDescending = !moduleSortDescending;
                sortDirectionButton.setText(moduleSortDescending ? "▼" : "▲");
                refreshModuleGrid();
            });
        }

        if (deadlineSortDirectionButton != null) {
            deadlineSortDirectionButton.setOnAction(e -> {
                deadlineSortDescending = !deadlineSortDescending;
                deadlineSortDirectionButton.setText(deadlineSortDescending ? "▼" : "▲");
                refreshDeadlineGrid();
            });
        }
    }

    private void speichereAenderungen() {
        if (speicherManager == null) return;
        // The ModulVerwaltung/DeadlineManager own private defensive copies of
        // the lists, so the SpeicherManager no longer shares their live state.
        // Push the managers' current state back into the SpeicherManager right
        // before persisting, so every add/edit/delete is written to disk.
        if (modulVerwaltung != null) {
            speicherManager.setModule(modulVerwaltung.getModule());
        }
        if (deadlineManager != null) {
            speicherManager.setDeadlines(deadlineManager.getDeadlines());
        }
        try {
            speicherManager.speichereDaten();
        } catch (IOException e) {
            zeigeFehler("Speicherfehler", "Die Daten konnten nicht gespeichert werden.", e.getMessage());
        }
    }

    private void zeigeFehler(String titel, String header, String inhalt) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titel);
        alert.setHeaderText(header);
        alert.setContentText(inhalt);
        alert.showAndWait();
    }

    private boolean bestaetigeLoeschen(String titel, String header, String name) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titel);
        alert.setHeaderText(header);
        alert.setContentText("Möchten Sie \"" + name + "\" endgültig entfernen?");
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private void refreshModuleGrid() {
        if (moduleGrid == null || modulVerwaltung == null) return;
        moduleGrid.getChildren().clear();

        modulVerwaltung.getModule().stream()
                .filter(this::matchesSearch)
                .filter(this::matchesStatusFilter)
                .filter(this::matchesSemesterFilter)
                .sorted(aktuellerModulComparator())
                .forEach(m -> moduleGrid.getChildren().add(createModuleCard(m)));
    }

    private boolean matchesSearch(Modul m) {
        if (searchQuery.isEmpty()) return true;
        if (m.getName().toLowerCase(Locale.GERMAN).contains(searchQuery)) return true;
        if (m.getSemester() != null && m.getSemester().getBezeichnung().toLowerCase(Locale.GERMAN).contains(searchQuery)) {
            return true;
        }
        return m.getSemester() != null && m.getSemester().name().toLowerCase(Locale.GERMAN).contains(searchQuery);
    }

    private boolean matchesStatusFilter(Modul m) {
        String filter = statusFilterBox == null ? null : statusFilterBox.getValue();
        if (filter == null || ALLE.equals(filter)) return true;
        boolean bestanden = m.getLeistung() != null && m.getLeistung().isBestanden();
        return "Bestanden".equals(filter) == bestanden;
    }

    private boolean matchesSemesterFilter(Modul m) {
        String filter = semesterFilterBox == null ? null : semesterFilterBox.getValue();
        if (filter == null || ALLE_SEMESTER.equals(filter)) return true;
        return m.getSemester() != null && filter.equals(m.getSemester().getBezeichnung());
    }

    private Comparator<Modul> aktuellerModulComparator() {
        String field = sortFieldBox == null ? null : sortFieldBox.getValue();
        Comparator<Modul> comparator = switch (field == null ? "Name" : field) {
            case "ECTS" -> Comparator.comparingInt(Modul::getEcts);
            case "Note" -> Comparator.comparingDouble(this::noteFuerSortierung);
            default -> Comparator.comparing(Modul::getName, String.CASE_INSENSITIVE_ORDER);
        };
        return moduleSortDescending ? comparator.reversed() : comparator;
    }

    private double noteFuerSortierung(Modul m) {
        if (m.getLeistung() instanceof Pruefungsleistung p) {
            return p.getErreichteNote();
        }
        return Double.MAX_VALUE;
    }

    private VBox createModuleCard(Modul m) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card");

        Label nameLabel = new Label(m.getName());
        nameLabel.getStyleClass().add("card-title");

        Label ectsLabel = new Label(m.getEcts() + " ECTS");
        ectsLabel.getStyleClass().add("card-subtitle");

        String semesterText = m.getSemester() != null ? m.getSemester().getBezeichnung() : "Kein Semester";
        Label semLabel = new Label(semesterText);
        semLabel.getStyleClass().add("card-subtitle");

        Label leistungLabel = new Label(formatLeistungInfo(m));
        leistungLabel.getStyleClass().add("card-subtitle");

        HBox buttonBar = new HBox(8);
        buttonBar.getStyleClass().add("button-bar");

        Button editBtn = new Button("Bearbeiten");
        editBtn.setOnAction(e -> bearbeiteModul(m));

        Button deleteBtn = new Button("Löschen");
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setOnAction(e -> {
            if (bestaetigeLoeschen("Modul löschen", "Modul wirklich löschen?", m.getName())) {
                modulVerwaltung.deleteModul(m.getId());
                refreshModuleGrid();
                speichereAenderungen();
            }
        });

        buttonBar.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(nameLabel, ectsLabel, semLabel, leistungLabel, buttonBar);
        return card;
    }

    private String formatLeistungInfo(Modul m) {
        if (m.getLeistung() instanceof Pruefungsleistung p) {
            return "Note: " + p.getErreichteNote() + (p.isBestanden() ? " (Bestanden)" : " (Nicht bestanden)");
        }
        if (m.getLeistung() instanceof Studienleistung s) {
            return "Studienleistung: " + (s.isBestanden() ? "Bestanden" : "Nicht bestanden");
        }
        return "Keine Leistung";
    }

    @FXML
    public void addModuleButtonOnAction(ActionEvent actionEvent) {
        showModulDialog(null).ifPresent(m -> {
            modulVerwaltung.addModul(m);
            refreshModuleGrid();
            speichereAenderungen();
        });
    }

    private void bearbeiteModul(Modul existing) {
        showModulDialog(existing).ifPresent(updated -> {
            modulVerwaltung.updateModul(updated);
            refreshModuleGrid();
            speichereAenderungen();
        });
    }

    private Optional<Modul> showModulDialog(Modul existing) {
        boolean isEdit = existing != null;
        Dialog<Modul> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modul bearbeiten" : "Neues Modul hinzufügen");
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
        ectsField.setPromptText("ECTS (z. B. 5)");
        ComboBox<Semester> semesterBox = new ComboBox<>();
        semesterBox.getItems().addAll(Semester.values());
        CheckBox benotetBox = new CheckBox("Ist benotet?");

        ComboBox<String> leistungTypeBox = new ComboBox<>();
        leistungTypeBox.getItems().addAll("Keine", "Prüfungsleistung", "Studienleistung");
        leistungTypeBox.setValue("Keine");

        TextField noteField = new TextField();
        noteField.setPromptText("Note (z. B. 1,3)");
        noteField.setDisable(true);

        CheckBox bestandenBox = new CheckBox("Bestanden");
        bestandenBox.setDisable(true);

        if (isEdit) {
            nameField.setText(existing.getName());
            ectsField.setText(String.valueOf(existing.getEcts()));
            semesterBox.setValue(existing.getSemester());
            benotetBox.setSelected(existing.istBenotet());
            if (existing.getLeistung() instanceof Pruefungsleistung p) {
                leistungTypeBox.setValue("Prüfungsleistung");
                noteField.setText(String.valueOf(p.getErreichteNote()));
                noteField.setDisable(false);
            } else if (existing.getLeistung() instanceof Studienleistung s) {
                leistungTypeBox.setValue("Studienleistung");
                bestandenBox.setSelected(s.isBestanden());
                bestandenBox.setDisable(false);
            }
        }

        final boolean[] syncing = {false};

        leistungTypeBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            noteField.setDisable(!"Prüfungsleistung".equals(newVal));
            bestandenBox.setDisable(!"Studienleistung".equals(newVal));
            if (syncing[0]) return;
            syncing[0] = true;
            if ("Prüfungsleistung".equals(newVal)) {
                benotetBox.setSelected(true);
            } else if ("Studienleistung".equals(newVal)) {
                benotetBox.setSelected(false);
            }
            syncing[0] = false;
        });

        benotetBox.selectedProperty().addListener((obs, oldVal, isBenotet) -> {
            if (syncing[0]) return;
            syncing[0] = true;
            String current = leistungTypeBox.getValue();
            if (isBenotet && !"Prüfungsleistung".equals(current)) {
                leistungTypeBox.setValue("Prüfungsleistung");
            } else if (!isBenotet && "Prüfungsleistung".equals(current)) {
                leistungTypeBox.setValue("Keine");
            }
            syncing[0] = false;
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

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            Optional<String> nameError = EingabeValidierung.validiereModulName(nameField.getText());
            if (nameError.isPresent()) {
                zeigeFehler("Eingabefehler", "Fehlerhafte Eingabe", nameError.get());
                event.consume();
                return;
            }
            Optional<String> ectsError = EingabeValidierung.validiereEcts(ectsField.getText());
            if (ectsError.isPresent()) {
                zeigeFehler("Eingabefehler", "Fehlerhafte Eingabe", ectsError.get());
                event.consume();
                return;
            }
            if ("Prüfungsleistung".equals(leistungTypeBox.getValue())) {
                Optional<String> noteError = EingabeValidierung.validiereNote(noteField.getText());
                if (noteError.isPresent()) {
                    zeigeFehler("Eingabefehler", "Fehlerhafte Eingabe", noteError.get());
                    event.consume();
                }
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton != saveButtonType) return null;

            String name = nameField.getText().trim();
            int ects = Integer.parseInt(ectsField.getText().trim());
            boolean istBenotet = benotetBox.isSelected();
            Semester semester = semesterBox.getValue();
            String lType = leistungTypeBox.getValue();

            Modul m = isEdit
                    ? new Modul(existing.getId(), name, ects, istBenotet, semester)
                    : new Modul(name, ects, istBenotet, semester);

            if ("Prüfungsleistung".equals(lType)) {
                m.setLeistung(new Pruefungsleistung(m, EingabeValidierung.parseNote(noteField.getText())));
            } else if ("Studienleistung".equals(lType)) {
                m.setLeistung(new Studienleistung(m, bestandenBox.isSelected()));
            }

            return m;
        });

        return dialog.showAndWait();
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

        List<Deadline> deadlines = deadlineManager.getAllDeadlinesSortiert();
        if (deadlineSortDescending) {
            Collections.reverse(deadlines);
        }
        for (Deadline d : deadlines) {
            deadlineGrid.getChildren().add(createDeadlineCard(d));
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
            speichereAenderungen();
        });
        doneBtn.setDisable(d.istErledigt());

        Button deleteBtn = new Button("Löschen");
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setOnAction(e -> {
            if (bestaetigeLoeschen("Frist löschen", "Frist wirklich löschen?", d.getTitel())) {
                deadlineManager.deleteDeadline(d);
                refreshDeadlineGrid();
                speichereAenderungen();
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

    @FXML
    public void addDeadlineButtonOnAction(ActionEvent actionEvent) {
        showDeadlineDialog(null).ifPresent(d -> {
            deadlineManager.addDeadline(d);
            refreshDeadlineGrid();
            speichereAenderungen();
        });
    }

    private void bearbeiteDeadline(Deadline existing) {
        showDeadlineDialog(existing).ifPresent(updated -> {
            existing.setBeschreibung(updated.getBeschreibung());
            existing.setDatum(updated.getDatum());
            existing.setTyp(updated.getTyp());
            existing.setModulName(updated.getModulName());
            refreshDeadlineGrid();
            speichereAenderungen();
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
                zeigeFehler("Eingabefehler", "Fehlerhafte Eingabe", beschreibungError.get());
                event.consume();
                return;
            }
            Optional<String> datumError = EingabeValidierung.validiereDatum(datePicker.getValue());
            if (datumError.isPresent()) {
                zeigeFehler("Eingabefehler", "Fehlerhafte Eingabe", datumError.get());
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

    @FXML
    public void onGradesTabSelected(Event event) {
        refreshGradesTab();
    }

    private void refreshGradesTab() {
        if (modulVerwaltung == null) return;

        LeistungsRechner rechner = new LeistungsRechner(modulVerwaltung.getModule());

        double gpa = rechner.berechneNotendurchschnitt();
        gpaLabel.setText(String.format(Locale.GERMAN, "%.2f", gpa));

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
