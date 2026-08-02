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
        for (Modul m : module) {
            if (m.getId().equals(id)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Modul mit ID " + id + " nicht gefunden.");
    }

    public void updateModul(Modul aktualisiertesModul) {
        Modul existierendes = getModul(aktualisiertesModul.getId()); // Wirft Exception falls nicht vorhanden
        existierendes.setName(aktualisiertesModul.getName());
        existierendes.setEcts(aktualisiertesModul.getEcts());
        existierendes.setIstBenotet(aktualisiertesModul.istBenotet());
        existierendes.setSemester(aktualisiertesModul.getSemester());
        existierendes.setLeistung(aktualisiertesModul.getLeistung());
    }

    public void deleteModul(String id) {
        Modul zuLoeschen = getModul(id); // Wirft Exception falls nicht vorhanden
        module.remove(zuLoeschen);
    }
}
