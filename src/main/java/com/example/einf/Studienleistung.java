package com.example.einf;

public class Studienleistung extends Leistung {
    private boolean bestanden;

    public Studienleistung(Modul modul, boolean bestanden) {
        super(modul);
        this.bestanden = bestanden;
    }

    @Override
    public double getErreichteNote() {
        return 0.0; // Keine Note bei Studienleistung
    }

    @Override
    public boolean isBestanden() {
        return bestanden;
    }

    public void setBestanden(boolean bestanden) {
        this.bestanden = bestanden;
    }
}
