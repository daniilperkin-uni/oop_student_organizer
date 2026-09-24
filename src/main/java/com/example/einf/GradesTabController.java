package com.example.einf;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Owns everything specific to the "Noten" (grades) tab: GPA and ECTS
 * statistics plus the per-module grades bar chart.
 *
 * <p>
 * Extracted from the former 674-line HomeController; wired programmatically
 * by HomeController. Refreshes on demand when the tab becomes visible.
 * </p>
 */
class GradesTabController {

    private Label gpaLabel;
    private Label ectsLabel;
    private BarChart<String, Number> gradesChart;
    private PieChart ectsChart;

    private ModulVerwaltung modulVerwaltung;

    void setGpaLabel(Label gpaLabel) {
        this.gpaLabel = gpaLabel;
    }

    void setEctsLabel(Label ectsLabel) {
        this.ectsLabel = ectsLabel;
    }

    void setGradesChart(BarChart<String, Number> gradesChart) {
        this.gradesChart = gradesChart;
    }

    void setEctsChart(PieChart ectsChart) {
        this.ectsChart = ectsChart;
    }

    void setModulVerwaltung(ModulVerwaltung modulVerwaltung) {
        this.modulVerwaltung = modulVerwaltung;
    }

    void onTabSelected() {
        refreshGradesTab();
    }

    /**
     * Opens the grade simulator dialog: the user enters a wish grade and sees
     * both the projected overall average (assuming open graded modules land
     * at the wish grade) and the average required in the open modules to hit
     * an exact target.
     */
    void showGradeSimulator() {
        if (modulVerwaltung == null) return;
        LeistungsRechner rechner = new LeistungsRechner(modulVerwaltung.getModule());

        TextInputDialog dialog = new TextInputDialog("2,0");
        dialog.setTitle("Notensimulator");
        dialog.setHeaderText("Was wäre wenn ...?");
        dialog.setContentText(
                "Angenommene Note für alle offenen benoteten Module "
                + "(z. B. 2,0):");

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            double wunsch = EingabeValidierung.parseNote(dialog.getEditor().getText());
            if (wunsch < 1.0 || wunsch > 4.0) {
                UiDialogs.zeigeFehler("Eingabefehler", "Ungültige Note",
                        "Bitte eine Note zwischen 1,0 und 4,0 eingeben.");
                event.consume();
            }
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        double wunsch = EingabeValidierung.parseNote(result.get());

        double aktuell = rechner.berechneNotendurchschnitt();
        StringBuilder text = new StringBuilder();
        text.append(String.format(Locale.GERMAN,
                "Aktueller Schnitt (nur benotete Module mit Note): %.2f%n%n", aktuell));

        Optional<Double> projektion = rechner.simuliereNotendurchschnitt(wunsch);
        projektion.ifPresentOrElse(
                p -> text.append(String.format(Locale.GERMAN,
                        "Projizierter Gesamtschnitt bei %.1f in offenen Modulen: %.2f%n",
                        wunsch, p)),
                () -> text.append("Keine offenen benoteten Module - es gibt nichts zu simulieren."));

        // For each interesting target show what would be needed.
        for (double ziel = 1.0; ziel <= 3.0; ziel += 0.5) {
            Optional<Double> noetig = rechner.benoetigteNoteFuerZiel(ziel);
            if (noetig.isPresent()) {
                text.append(String.format(Locale.GERMAN,
                        "Für Endnote %.1f: Schnitt von %.2f in den offenen Modulen nötig.%n",
                        ziel, noetig.get()));
            } else {
                text.append(String.format(Locale.GERMAN,
                        "Für Endnote %.1f: nicht mehr erreichbar.%n", ziel));
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notensimulator");
        alert.setHeaderText("Notenprojektion");
        alert.setContentText(text.toString());
        alert.showAndWait();
    }

    /**
     * Zeigt das Semester-Dashboard: ECTS-Fortschritt und Notenschnitt je
     * Semester als uebersichtlicher Text-Report in einem Dialog.
     */
    void showSemesterDashboard() {
        if (modulVerwaltung == null) return;
        SemesterStatistik statistik = new SemesterStatistik(modulVerwaltung.getModule());
        List<SemesterStatistik.Datensatz> datensaetze = statistik.datensaetze();
        if (datensaetze.isEmpty()) {
            UiDialogs.zeigeFehler("Semester-Dashboard", "Nichts anzuzeigen",
                    "Es sind keine Module erfasst.");
            return;
        }

        StringBuilder text = new StringBuilder();
        for (SemesterStatistik.Datensatz d : datensaetze) {
            long balken = Math.round(d.fortschrittProzent() / 10.0);
            text.append(d.bezeichnung()).append("\n");
            text.append("[")
                    .append("█".repeat((int) balken))
                    .append("░".repeat(10 - (int) balken))
                    .append("] ")
                    .append(String.format(Locale.GERMAN, "%.0f%%", d.fortschrittProzent()))
                    .append("  (")
                    .append(d.bestandeneEcts()).append(" / ").append(d.gesamtEcts()).append(" ECTS");
            if (d.durchschnitt() > 0) {
                text.append(String.format(Locale.GERMAN, ", Ø %.2f", d.durchschnitt()));
            }
            text.append(")\n\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Semester-Dashboard");
        alert.setHeaderText("Fortschritt je Semester");
        alert.getDialogPane().setStyle("-fx-font-family: monospace;");
        alert.setContentText(text.toString());
        alert.showAndWait();
    }

    private void refreshGradesTab() {
        if (modulVerwaltung == null) return;

        LeistungsRechner rechner = new LeistungsRechner(modulVerwaltung.getModule());

        double gpa = rechner.berechneNotendurchschnitt();
        gpaLabel.setText(String.format(Locale.GERMAN, "%.2f", gpa));

        int passedEcts = rechner.berechneBestandeneEcts();
        int totalEcts = rechner.berechneGesamtEcts();
        ectsLabel.setText(passedEcts + " / " + totalEcts);

        if (ectsChart != null) {
            EctsFortschritt f = EctsFortschritt.berechne(modulVerwaltung.getModule());
            ectsChart.getData().setAll(
                    new PieChart.Data("Bestanden (" + f.bestanden() + ")", f.bestanden()),
                    new PieChart.Data("Nicht bestanden (" + f.nichtBestanden() + ")", f.nichtBestanden()),
                    new PieChart.Data("Offen (" + f.offen() + ")", f.offen()));
            ectsChart.setTitle(String.format(Locale.GERMAN, "ECTS-Fortschritt: %.0f %%", f.prozentBestanden()));
        }

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
