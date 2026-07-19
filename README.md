# Studentischer Organisationshelfer EinfVersuch2

Ein Hilfswerkzeug für den studentischen Alltag, entwickelt in Java. 
Dieses Programm hilft Studierenden dabei, Fristen besser zu organisieren, Module zu verwalten und den eigenen Leistungsstand (Noten und ECTS) stets im Blick zu behalten. 

Entwickelt unter Anwendung aller gelernten Konzepte der objektorientierten Programmierung (OOP).

## Kernfunktionen (Die 5 Pfeiler)

* **Verwaltung von Modulen:** Fächer inkl. ECTS und Status anlegen, bearbeiten und verwalten.
* **Deadline-Tracking:** Anmelde- und Abgabefristen sowie Klausurtermine überwachen (mit Warnfunktion).
* **Leistungsüberwachung:** Automatische Berechnung des gewichteten Notendurchschnitts und des Studienfortschritts.
* **Persistente Datenhaltung:** Sicheres Speichern und Laden der Nutzerdaten auf der Festplatte.
* **Grafische Benutzeroberfläche (GUI):** Intuitive Bedienung durch eine moderne und übersichtliche Oberfläche.

## Technologien & Architektur

* **Sprache:** Java (JDK 21+)
* **GUI-Framework:** JavaFX (via FXML)
* **Build-Tool:** Maven
* **Architektur:** Model-View-Controller (MVC) zur sauberen Trennung von Daten, Logik und Oberfläche.

## Starten des Programms

Um das Programm lokal auszuführen, wird Maven benötigt.

```bash
# Projekt kompilieren und ausführen
.\mvnw.cmd clean javafx:run
```
