package com.example.einf;

import java.util.UUID;

/**
 * Repräsentiert ein Universitätsmodul.
 */
public class Modul {

    private String id;
    private String name;
    private int ects;
    private boolean istBenotet;
    private Semester semester;
    private Leistung leistung;

    public Modul(String name, int ects, boolean istBenotet, Semester semester) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.ects = ects;
        this.istBenotet = istBenotet;
        this.semester = semester;
    }
    
    // Konstruktor mit ID für das Laden aus Dateien
    public Modul(String id, String name, int ects, boolean istBenotet, Semester semester) {
        this.id = id;
        this.name = name;
        this.ects = ects;
        this.istBenotet = istBenotet;
        this.semester = semester;
    }

    public String getId() { return id; }
    /**
     * Package-private on purpose: the id is assigned in the constructor
     * (UUID-generated, or restored from storage). Allowing public mutation
     * would break the immutability invariant of the id after construction.
     */
    void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getEcts() { return ects; }
    public void setEcts(int ects) { this.ects = ects; }

    public boolean istBenotet() { return istBenotet; }
    public void setIstBenotet(boolean istBenotet) { this.istBenotet = istBenotet; }

    public Semester getSemester() { return semester; }
    public void setSemester(Semester semester) { this.semester = semester; }

    public Leistung getLeistung() { return leistung; }
    public void setLeistung(Leistung leistung) { this.leistung = leistung; }

    @Override
    public String toString() {
        return "Modul{id='" + id + "', name='" + name + "', ects=" + ects + 
               ", istBenotet=" + istBenotet + ", semester=" + semester + "}";
    }
}
