package com.example.einf;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

/**
 * Owns everything specific to the "Module" tab: search, filtering, sorting,
 * the module card grid and the add/edit module dialog.
 *
 * <p>
 * Extracted from the former 674-line HomeController. The class is wired
 * programmatically by HomeController (which keeps the @FXML injections) and
 * receives the module management plus a save callback so every mutating
 * action persists through the same path as before the split.
 * </p>
 */
class ModuleTabController {

    private static final String ALLE = "Alle";
    private static final String ALLE_SEMESTER = "Alle Semester";

    private FlowPane moduleGrid;
    private TextField searchBar;
    private ComboBox<String> statusFilterBox;
    private ComboBox<String> semesterFilterBox;
    private ComboBox<String> sortFieldBox;
    private Button sortDirectionButton;

    private ModulVerwaltung modulVerwaltung;
    private Runnable onSave;

    private String searchQuery = "";
    private boolean moduleSortDescending = false;

    void setModuleGrid(FlowPane moduleGrid) {
        this.moduleGrid = moduleGrid;
    }

    void setSearchBar(TextField searchBar) {
        this.searchBar = searchBar;
    }

    void setStatusFilterBox(ComboBox<String> statusFilterBox) {
        this.statusFilterBox = statusFilterBox;
    }

    void setSemesterFilterBox(ComboBox<String> semesterFilterBox) {
        this.semesterFilterBox = semesterFilterBox;
    }

    void setSortFieldBox(ComboBox<String> sortFieldBox) {
        this.sortFieldBox = sortFieldBox;
    }

    void setSortDirectionButton(Button sortDirectionButton) {
        this.sortDirectionButton = sortDirectionButton;
    }

    void setModulVerwaltung(ModulVerwaltung modulVerwaltung) {
        this.modulVerwaltung = modulVerwaltung;
        refreshModuleGrid();
    }

    void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    void initializeSearch() {
        if (searchBar != null) {
            searchBar.textProperty().addListener((obs, oldVal, newVal) -> {
                searchQuery = newVal == null ? "" : newVal.trim().toLowerCase(Locale.GERMAN);
                refreshModuleGrid();
            });
        }
    }

    void clearSearch() {
        if (searchBar != null) {
            searchBar.clear();
        }
    }

    void initializeFilters() {
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
    }

    void addModuleButtonOnAction(ActionEvent actionEvent) {
        showModulDialog(null).ifPresent(m -> {
            modulVerwaltung.addModul(m);
            refreshModuleGrid();
            saveChanges();
        });
    }

    private void saveChanges() {
        if (onSave != null) {
            onSave.run();
        }
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
            if (UiDialogs.bestaetigeLoeschen("Modul löschen", "Modul wirklich löschen?", m.getName())) {
                modulVerwaltung.deleteModul(m.getId());
                refreshModuleGrid();
                saveChanges();
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

    private void bearbeiteModul(Modul existing) {
        showModulDialog(existing).ifPresent(updated -> {
            modulVerwaltung.updateModul(updated);
            refreshModuleGrid();
            saveChanges();
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
                UiDialogs.zeigeFehler("Eingabefehler", "Fehlerhafte Eingabe", nameError.get());
                event.consume();
                return;
            }
            Optional<String> ectsError = EingabeValidierung.validiereEcts(ectsField.getText());
            if (ectsError.isPresent()) {
                UiDialogs.zeigeFehler("Eingabefehler", "Fehlerhafte Eingabe", ectsError.get());
                event.consume();
                return;
            }
            if ("Prüfungsleistung".equals(leistungTypeBox.getValue())) {
                Optional<String> noteError = EingabeValidierung.validiereNote(noteField.getText());
                if (noteError.isPresent()) {
                    UiDialogs.zeigeFehler("Eingabefehler", "Fehlerhafte Eingabe", noteError.get());
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
}
