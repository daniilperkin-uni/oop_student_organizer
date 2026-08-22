package com.example.einf;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.util.Locale;

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

    void setModulVerwaltung(ModulVerwaltung modulVerwaltung) {
        this.modulVerwaltung = modulVerwaltung;
    }

    void onTabSelected() {
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
