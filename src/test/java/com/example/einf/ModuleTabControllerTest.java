package com.example.einf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests fuer die JavaFX-freien Teile der Modulliste: Statusfilter und Sortierung.
 */
class ModuleTabControllerTest {

    private Modul offen(String name) {
        return new Modul(name, 5, true, null);
    }

    private Modul mitNote(String name, double note) {
        Modul m = new Modul(name, 5, true, null);
        m.setLeistung(new Pruefungsleistung(m, note));
        return m;
    }

    @Test
    @DisplayName("Der Filter \"Nicht bestanden\" zeigt keine offenen Module")
    void nichtBestandenZeigtKeineOffenenModule() {
        // Ohne Leistung ist ein Modul weder bestanden noch durchgefallen. Vorher
        // galt !bestanden, wodurch der Filter alle offenen Module einsammelte.
        assertFalse(ModuleTabController.matchesStatus(offen("Offen"), "Nicht bestanden"));
        assertTrue(ModuleTabController.matchesStatus(mitNote("Durch", 5.0), "Nicht bestanden"));
        assertFalse(ModuleTabController.matchesStatus(mitNote("Best", 2.0), "Nicht bestanden"));
    }

    @Test
    @DisplayName("Der Filter \"Bestanden\" zeigt nur bestandene Module")
    void bestandenZeigtNurBestandene() {
        assertTrue(ModuleTabController.matchesStatus(mitNote("Best", 2.0), "Bestanden"));
        assertFalse(ModuleTabController.matchesStatus(mitNote("Durch", 5.0), "Bestanden"));
        assertFalse(ModuleTabController.matchesStatus(offen("Offen"), "Bestanden"));
    }

    @Test
    @DisplayName("\"Alle\" laesst alles durch, auch Module ohne Leistung")
    void alleLaesstAllesDurch() {
        assertTrue(ModuleTabController.matchesStatus(offen("Offen"), "Alle"));
        assertTrue(ModuleTabController.matchesStatus(offen("Offen"), null));
    }

    @Test
    @DisplayName("Ohne Note sortiert ein Modul in beiden Richtungen ans Ende")
    void ohneNoteImmerAmEnde() {
        Modul eins = mitNote("A", 1.0);
        Modul drei = mitNote("B", 3.0);
        Modul ohne = offen("Ohne");

        List<Modul> aufsteigend = new ArrayList<>(List.of(ohne, drei, eins));
        aufsteigend.sort(ModuleTabController.sortierung("Note", false));
        assertEquals(List.of(eins, drei, ohne), aufsteigend);

        List<Modul> absteigend = new ArrayList<>(List.of(ohne, drei, eins));
        absteigend.sort(ModuleTabController.sortierung("Note", true));
        assertEquals(List.of(drei, eins, ohne), absteigend);
    }
}
