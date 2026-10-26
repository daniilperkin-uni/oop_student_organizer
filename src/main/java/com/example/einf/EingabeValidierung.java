package com.example.einf;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Zentrale Validierung für Benutzereingaben.
 */
public final class EingabeValidierung {

    /** Obergrenze fuer die ECTS eines einzelnen Moduls. */
    private static final int MAX_ECTS = 30;

    private EingabeValidierung() {}

    public static Optional<String> validiereModulName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.of("Der Modulname darf nicht leer sein.");
        }
        return Optional.empty();
    }

    public static Optional<String> validiereEcts(String text) {
        if (text == null || text.isBlank()) {
            return Optional.of("ECTS müssen angegeben werden.");
        }
        try {
            int ects = Integer.parseInt(text.trim());
            // 30 ist die Obergrenze eines einzelnen Moduls. Ohne sie machte
            // eine vertippte Ziffer (z. B. 60 statt 6) ein Modul zum
            // Schwergewicht und verzerrte jeden ECTS-gewichteten Schnitt und
            // den ECTS-Fortschritt dauerhaft.
            if (ects <= 0 || ects > MAX_ECTS) {
                return Optional.of("ECTS müssen zwischen 1 und " + MAX_ECTS + " liegen.");
            }
            return Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.of("ECTS müssen eine ganze Zahl sein.");
        }
    }

    public static Optional<String> validiereNote(String text) {
        if (text == null || text.isBlank()) {
            return Optional.of("Bitte geben Sie eine Note ein.");
        }
        try {
            double note = Double.parseDouble(text.trim().replace(',', '.'));
            // NaN/Infinity bestehen jeden Größenvergleich (jeder Vergleich mit
            // NaN ist false) und würden bis in die CSV-Datei durchrutschen.
            if (!Double.isFinite(note) || note < 1.0 || note > 5.0) {
                return Optional.of("Die Note muss zwischen 1,0 und 5,0 liegen.");
            }
            return Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.of("Die Note muss eine Zahl sein (z. B. 1,3).");
        }
    }

    public static Optional<String> validiereDeadlineBeschreibung(String text) {
        if (text == null || text.isBlank()) {
            return Optional.of("Die Beschreibung darf nicht leer sein.");
        }
        return Optional.empty();
    }

    public static Optional<String> validiereDatum(LocalDate datum) {
        if (datum == null) {
            return Optional.of("Bitte wählen Sie ein Datum aus.");
        }
        return Optional.empty();
    }

    public static double parseNote(String text) {
        return Double.parseDouble(text.trim().replace(',', '.'));
    }
}
