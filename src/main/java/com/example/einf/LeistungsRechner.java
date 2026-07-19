package com.example.einf;

import java.util.List;

/**
 * Berechnet Leistungskennzahlen auf Basis einer Liste von Modulen.
 */
public class LeistungsRechner {

    private final List<Modul> module;

    public LeistungsRechner(List<Modul> module) {
        if (module == null) throw new IllegalArgumentException("Modulliste darf nicht null sein.");
        this.module = module;
    }

    /**
     * Berechnet den gewichteten Notendurchschnitt über alle benoteten Module.
     * 
     * @return gewichteter Schnitt, oder {@code 0.0} wenn keine benoteten Module vorliegen
     */
    public double berechneNotendurchschnitt() {
        double summeNoteEcts = 0.0;
        int summeBenoteteEcts = 0;

        for (Modul m : module) {
            if (m.hatNote()) {
                summeNoteEcts += m.getNote() * m.getEcts();
                summeBenoteteEcts += m.getEcts();
            }
        }

        if (summeBenoteteEcts == 0) return 0.0;
        return summeNoteEcts / summeBenoteteEcts;
    }

    /**
     * Berechnet die Summe der erfolgreich bestandenen ECTS-Punkte.
     * 
     * @return Anzahl der bestandenen ECTS
     */
    public int berechneBestandeneEcts() {
        int summe = 0;
        for (Modul m : module) {
            if (istBestanden(m)) {
                summe += m.getEcts();
            }
        }
        return summe;
    }

    /**
     * Gibt die Anzahl der bestandenen Module zurück.
     */
    public long anzahlBestandenerModule() {
        return module.stream().filter(this::istBestanden).count();
    }

    /**
     * Gibt die Gesamtzahl der ECTS aller erfassten Module zurück.
     */
    public int berechneGesamtEcts() {
        return module.stream().mapToInt(Modul::getEcts).sum();
    }

    /**
     * Prüft, ob ein einzelnes Modul als bestanden gilt.
     */
    public boolean istBestanden(Modul m) {
        if (m.getStatus() == ModulStatus.BESTANDEN) return true;
        return m.hatNote() && m.getNote() <= 4.0;
    }
}
