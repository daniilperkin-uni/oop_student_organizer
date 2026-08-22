package com.example.einf;

import java.util.List;
import java.util.Optional;

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
            if (m.istBenotet() && m.getLeistung() != null && m.getLeistung() instanceof Pruefungsleistung) {
                summeNoteEcts += m.getLeistung().getErreichteNote() * m.getEcts();
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
        return m.getLeistung() != null && m.getLeistung().isBestanden();
    }

    /**
     * Notensimulator: Welche Endnote ergibt sich im Schnitt, wenn die noch
     * offenen benoteten Module mit der Wunschnote abschneiden?
     *
     * <p>
     * Offen bedeutet: benotet, aber noch keine Pruefungsleistung eingetragen.
     * Bereits erbrachte Pruefungsleistungen gehen gewichtet nach ECTS ein -
     * identisch zu {@link #berechneNotendurchschnitt()}.
     * </p>
     *
     * @param wunschnote angenommene Note fuer alle offenen benoteten Module
     *                   (z. B. 2.0); Werte ausserhalb 1.0-4.0 werden nicht
     *                    simuliert und liefern Optional.empty()
     * @return projizierter Gesamtschnitt oder empty wenn nichts offen ist bzw.
     *         die Wunschnote ausserhalb des gueltigen Bereichs liegt
     */
    public Optional<Double> simuliereNotendurchschnitt(double wunschnote) {
        if (wunschnote < 1.0 || wunschnote > 4.0) return Optional.empty();

        double summeNoteEcts = 0.0;
        int summeBenoteteEcts = 0;

        for (Modul m : module) {
            if (!m.istBenotet()) continue;
            boolean hatPruefung = m.getLeistung() != null && m.getLeistung() instanceof Pruefungsleistung;
            double note = hatPruefung ? m.getLeistung().getErreichteNote() : wunschnote;
            summeNoteEcts += note * m.getEcts();
            summeBenoteteEcts += m.getEcts();
        }

        if (summeBenoteteEcts == 0) return Optional.empty();
        return Optional.of(summeNoteEcts / summeBenoteteEcts);
    }

    /**
     * Notensimulator in Gegenrichtung: Welche Note darf in den offenen
     * benoteten Modulen im Schnitt herauskommen, damit am Ende genau die
     * Zielnote steht? Das ist die typische Frage "Was muss ich noch
     * schreiben, um auf 2.0 zu kommen?".
     *
     * <p>
     * Die Rechnung loest {@code (fix + x * offenEcts) / (fixEcts + offenEcts)
     * = zielnote} nach x auf. Liegt das Ergebnis ausserhalb 1.0-5.0, ist das
     * Ziel mit den offenen Modulen (mathematisch) nicht erreichbar bzw.
     * beliebig sicher - auch dann wird empty geliefert, damit die UI klar
     * "nicht darstellbar" zeigen kann.
     * </p>
     *
     * @param zielnote angestrebter Endnotenschnitt (1.0 bis 4.0 sinnvoll)
     * @return noetiger Schnitt in allen offenen benoteten Modulen, oder
     *         empty wenn es keine offenen benoteten Module gibt oder das
     *         Ergebnis ausserhalb 1.0-5.0 laege
     */
    public Optional<Double> benoetigteNoteFuerZiel(double zielnote) {
        if (zielnote < 1.0 || zielnote > 4.0) return Optional.empty();

        double fixNoteEcts = 0.0;
        int fixEcts = 0;
        int offenEcts = 0;

        for (Modul m : module) {
            if (!m.istBenotet()) continue;
            boolean hatPruefung = m.getLeistung() != null && m.getLeistung() instanceof Pruefungsleistung;
            if (hatPruefung) {
                fixNoteEcts += m.getLeistung().getErreichteNote() * m.getEcts();
                fixEcts += m.getEcts();
            } else {
                offenEcts += m.getEcts();
            }
        }

        if (offenEcts == 0) return Optional.empty();

        double rest = zielnote * (fixEcts + offenEcts) - fixNoteEcts;
        double noetig = rest / offenEcts;
        if (noetig < 1.0 || noetig > 5.0) return Optional.empty();
        return Optional.of(noetig);
    }
}
