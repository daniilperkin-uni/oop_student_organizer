package com.example.einf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModulVerwaltung {
    
    private final List<Modul> module;

    public ModulVerwaltung() {
        this.module = new ArrayList<>();
    }

    public ModulVerwaltung(List<Modul> module) {
        // Defensive copy: the manager owns its own list so callers cannot
        // mutate the backing store through the reference they passed in.
        this.module = new ArrayList<>(module);
    }

    /**
     * Returns an unmodifiable view of the modules. Callers can read and
     * iterate freely but cannot mutate the internal list directly; use
     * addModul/updateModul/deleteModul for changes.
     */
    public List<Modul> getModule() {
        return Collections.unmodifiableList(module);
    }

    public void addModul(Modul m) {
        if (m == null) throw new IllegalArgumentException("Modul darf nicht null sein.");
        module.add(m);
    }

    public Modul getModul(String id) {
        int index = indexOfModul(id);
        if (index < 0) {
            throw new IllegalArgumentException("Modul mit ID " + id + " nicht gefunden.");
        }
        return module.get(index);
    }

    /**
     * Ersetzt das vorhandene Modul mit derselben ID durch das uebergebene
     * Objekt statt Felder einzeln zu kopieren. Eine manuelle Feldkopie
     * verliert stillschweigend jedes Feld, das {@link Modul} spaeter erhaelt;
     * der vollstaendige Austausch kann nicht auseinanderlaufen.
     *
     * <p>Das uebergebene Modul wird nicht kopiert: eine eventuell darin
     * gesetzte {@code Pruefungsleistung} verweist auf genau dieses Objekt,
     * eine Kopie wuerde den Objektgraphen brechen.</p>
     */
    public void updateModul(Modul aktualisiertesModul) {
        if (aktualisiertesModul == null) {
            throw new IllegalArgumentException("Modul darf nicht null sein.");
        }
        int index = indexOfModul(aktualisiertesModul.getId()); // -1 falls nicht vorhanden
        if (index < 0) {
            throw new IllegalArgumentException(
                    "Modul mit ID " + aktualisiertesModul.getId() + " nicht gefunden.");
        }
        module.set(index, aktualisiertesModul);
    }

    public void deleteModul(String id) {
        int index = indexOfModul(id); // -1 falls nicht vorhanden
        if (index < 0) {
            throw new IllegalArgumentException("Modul mit ID " + id + " nicht gefunden.");
        }
        module.remove(index);
    }

    /** @return Index des Moduls mit der uebergebenen id, oder -1 wenn keines existiert. */
    private int indexOfModul(String id) {
        for (int i = 0; i < module.size(); i++) {
            if (module.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
