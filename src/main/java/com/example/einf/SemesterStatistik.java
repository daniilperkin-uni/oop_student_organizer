package com.example.einf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregiert Module je Semester zu Fortschritts- und Notenkennzahlen.
 *
 * <p>
 * Reine Berechnungsklasse (kein JavaFX), damit die Aggregation unit-testbar
 * bleibt. Die Reihenfolge der Datensaetze folgt der Deklarationsreihenfolge
 * des Semester-Enums, "Kein Semester" steht am Ende.
 * </p>
 */
public class SemesterStatistik {

    /**
     * Kennzahlen fuer ein Semester.
     */
    public record Datensatz(
            String bezeichnung,
            int bestandeneEcts,
            int gesamtEcts,
            double durchschnitt,
            double fortschrittProzent) {
    }

    private final List<Datensatz> datensaetze = new ArrayList<>();

    public SemesterStatistik(List<Modul> module) {
        Map<Semester, List<Modul>> gruppen = new LinkedHashMap<>();
        for (Semester s : Semester.values()) {
            gruppen.put(s, new ArrayList<>());
        }
        List<Modul> ohneSemester = new ArrayList<>();

        for (Modul m : module) {
            if (m.getSemester() == null) {
                ohneSemester.add(m);
            } else {
                gruppen.get(m.getSemester()).add(m);
            }
        }

        for (Map.Entry<Semester, List<Modul>> e : gruppen.entrySet()) {
            if (e.getValue().isEmpty()) continue; // leere Semester ausblenden
            datensaetze.add(berechne(e.getKey().getBezeichnung(), e.getValue()));
        }
        if (!ohneSemester.isEmpty()) {
            datensaetze.add(berechne("Kein Semester", ohneSemester));
        }
    }

    private Datensatz berechne(String bezeichnung, List<Modul> module) {
        LeistungsRechner rechner = new LeistungsRechner(module);
        int bestanden = rechner.berechneBestandeneEcts();
        int gesamt = rechner.berechneGesamtEcts();
        double schnitt = rechner.berechneNotendurchschnitt();
        double fortschritt = gesamt == 0 ? 0.0 : (bestanden * 100.0) / gesamt;
        return new Datensatz(bezeichnung, bestanden, gesamt, schnitt, fortschritt);
    }

    public List<Datensatz> datensaetze() {
        return List.copyOf(datensaetze);
    }

    /**
     * Convenience fuer Tests und UI-Suche nach Bezeichnung.
     */
    public Datensatz finde(String bezeichnung) {
        return datensaetze.stream()
                .filter(d -> d.bezeichnung().equals(bezeichnung))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Kein Datensatz: " + bezeichnung));
    }
}
