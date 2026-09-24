package com.example.einf;

import java.util.List;

/**
 * Pure ECTS progress breakdown for the dashboard pie chart: passed ECTS,
 * failed ECTS and still open ECTS (modules without a result). No JavaFX types so
 * it can be unit-tested headless.
 */
record EctsFortschritt(int bestanden, int nichtBestanden, int offen) {

    static EctsFortschritt berechne(List<Modul> module) {
        int b = 0, nb = 0, o = 0;
        for (Modul m : module) {
            Leistung l = m.getLeistung();
            if (l == null) {
                o += m.getEcts();
            } else if (l.isBestanden()) {
                b += m.getEcts();
            } else {
                nb += m.getEcts();
            }
        }
        return new EctsFortschritt(b, nb, o);
    }

    int gesamt() {
        return bestanden + nichtBestanden + offen;
    }

    /** Share of passed ECTS in percent, 0 when no modules exist. */
    double prozentBestanden() {
        return gesamt() == 0 ? 0.0 : 100.0 * bestanden / gesamt();
    }
}
