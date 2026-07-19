package com.example.einf;

/**
 * Repräsentiert ein Universitätsmodul mit Namen, ECTS-Punkten, Status und optionaler Note.
 */
public class Modul {

    private String name;
    private int ects;
    private ModulStatus status;
    private double note; // 0.0 bedeutet: noch keine Note eingetragen

    public Modul(String name, int ects, ModulStatus status, double note) {
        this.name = name;
        this.ects = ects;
        this.status = status;
        this.note = note;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getEcts() { return ects; }
    public void setEcts(int ects) { this.ects = ects; }

    public ModulStatus getStatus() { return status; }
    public void setStatus(ModulStatus status) { this.status = status; }

    public double getNote() { return note; }
    public void setNote(double note) { this.note = note; }

    public boolean hatNote() {
        return note > 0.0;
    }

    @Override
    public String toString() {
        return "Modul{name='" + name + "', ects=" + ects + ", status=" + status + ", note=" + note + "}";
    }
}
