package com.example.einf;

public class Pruefungsleistung extends Leistung {
    private double erreichteNote;

    public Pruefungsleistung(Modul modul, double erreichteNote) {
        super(modul);
        this.erreichteNote = erreichteNote;
    }

    @Override
    public double getErreichteNote() {
        return erreichteNote;
    }

    public void setErreichteNote(double erreichteNote) {
        this.erreichteNote = erreichteNote;
    }

    @Override
    public boolean isBestanden() {
        return erreichteNote > 0.0 && erreichteNote <= 4.0;
    }
}
