package com.example.einf;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Zentrale Validierung für Benutzereingaben.
 */
public final class EingabeValidierung {

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
            if (ects <= 0) {
                return Optional.of("ECTS müssen größer als 0 sein.");
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
            if (note < 1.0 || note > 5.0) {
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
