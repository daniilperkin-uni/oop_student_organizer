package com.example.einf;

public abstract class Leistung {
    protected Modul modul;

    public Leistung(Modul modul) {
        if (modul == null) throw new IllegalArgumentException("Modul darf nicht null sein.");
        this.modul = modul;
    }

    public Modul getModul() {
        return modul;
    }

    public abstract boolean isBestanden();
    public abstract double getErreichteNote();
}
