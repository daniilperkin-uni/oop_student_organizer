# Studentischer Organisationshelfer

[![CI](https://github.com/daniilperkin-uni/oop_student_organizer/actions/workflows/ci.yml/badge.svg)](https://github.com/daniilperkin-uni/oop_student_organizer/actions/workflows/ci.yml)

Ein Hilfswerkzeug für den studentischen Alltag, entwickelt in Java. 
Dieses Programm hilft Studierenden dabei, Fristen besser zu organisieren, Module zu verwalten und den eigenen Leistungsstand (Noten und ECTS) stets im Blick zu behalten. 

Entwickelt unter Anwendung aller gelernten Konzepte der objektorientierten Programmierung (OOP).

## Live-Showcase

Dieses Projekt hat eine Sektion auf der Uni-Projekte-Showcase-Seite: **[Live im Browser ansehen](https://daniilperkin-uni.github.io/uni-old-projects/#oop_student_organizer)** – mit Screenshots der laufenden App (Module, Fristen, Noten) und einem interaktiven Notensimulator, dessen ECTS-gewichtete Berechnung 1:1 aus dem `LeistungsRechner` dieses Repos portiert ist.

## Kernfunktionen (Die 5 Pfeiler)

* **Verwaltung von Modulen:** Fächer inkl. ECTS und Semester anlegen, bearbeiten und verwalten – mit Suche, Filtern (Status, Semester) und Sortierung (Name, ECTS, Note).
* **Deadline-Tracking:** Anmelde- und Abgabefristen sowie Klausurtermine überwachen; jede Fristenkarte zeigt ihren Status (Offen / Überfällig / Erledigt) farblich an.
* **Leistungsüberwachung:** Automatischer, nach ECTS gewichteter Notendurchschnitt, ECTS-Fortschrittsdiagramm, Noten-Balkendiagramm je Modul, Semester-Dashboard mit Kennzahlen pro Semester und ein Notensimulator (Projektion des Endschnitts sowie „Welche Note brauche ich noch?“).
* **Persistente Datenhaltung:** Sicheres Speichern und Laden der Nutzerdaten auf der Festplatte.
* **Grafische Benutzeroberfläche (GUI):** Intuitive Bedienung durch eine moderne und übersichtliche Oberfläche.

## Technologien & Architektur

* **Sprache:** Java (JDK 21+)
* **GUI-Framework:** JavaFX (via FXML)
* **Build-Tool:** Maven
* **Architektur:** Model-View-Controller (MVC) zur sauberen Trennung von Daten, Logik und Oberfläche; `HomeController` koordiniert das Hauptfenster und delegiert je Tab an einen eigenen Controller.

## Voraussetzungen

* **JDK 21 oder neuer** (Entwicklung und CI: Temurin 21)
* Maven muss nicht installiert sein – der Maven Wrapper (`.mvn/wrapper/`, `mvnw`, `mvnw.cmd`) ist im Projekt enthalten.

## Starten des Programms

```bash
.\mvnw.cmd clean javafx:run   # Windows
./mvnw clean javafx:run       # macOS / Linux
```

> Hinweis: Die Anwendung wird ausschließlich über das `javafx:run`-Ziel gestartet. Das gebaute Jar enthält bewusst keinen `Main-Class`-Eintrag und ist nicht per `java -jar` startbar.

## Projektstruktur

* `src/main/java/com/example/einf/` – Domänen- und UI-Klassen
  * `Main.java` – Einstiegspunkt: lädt die Daten und verdrahtet Controller und Manager
  * `HomeController.java` – Koordinator des Hauptfensters, delegiert an `ModuleTabController`, `DeadlineTabController` und `GradesTabController`
  * Domänenklassen: `Modul`, `Semester`, `Leistung` (→ `Pruefungsleistung` / `Studienleistung`), `Deadline`, `DeadlineManager`, `ModulVerwaltung`, `LeistungsRechner`, `SemesterStatistik`, `EctsFortschritt`
  * Infrastruktur: `SpeicherManager` (CSV-Persistenz), `IcsExporter` (Kalenderexport), `EingabeValidierung`, `UiDialogs`
* `src/main/resources/com/example/einf/views/home-view.fxml` – FXML-Layout des Hauptfensters
* `src/main/resources/com/example/einf/styles/app.css` – Stylesheet
* `src/test/java/com/example/einf/` – 69 JUnit-Tests

## OOP-Konzepte im Code

* **Kapselung:** Die Verwaltungsklassen (`ModulVerwaltung`, `DeadlineManager`, `SpeicherManager`) arbeiten mit defensiven Kopien und geben nur unveränderliche Sichten ihrer Listen heraus – Änderungen laufen ausschließlich über ihre Methoden.
* **Vererbung & Polymorphismus:** `Leistung` ist abstrakt; `Pruefungsleistung` (Note) und `Studienleistung` (bestanden ja/nein) implementieren `isBestanden()` und `getErreichteNote()` unterschiedlich. Auswertung und Anzeige nutzen diese gemeinsamen Methoden.
* **Trennung von Logik und Oberfläche (MVC):** Die Berechnungsklassen (`LeistungsRechner`, `SemesterStatistik`, `EctsFortschritt`) sind bewusst JavaFX-frei und dadurch direkt unit-testbar.
* **Robuste Datenformate:** Der `SpeicherManager` implementiert ein eigenes, escape-fähiges CSV-Format inkl. `.bak`-Sicherung; der `IcsExporter` erzeugt RFC-5545-konforme Kalenderdateien (CRLF-Zeilenenden, Escaping, Ganztagestermine).

## Datenhaltung

Alle Daten liegen im Verzeichnis `~/.studenthelfer` (unter Windows `%USERPROFILE%\.studenthelfer`):

* `module.csv` – Module inkl. ECTS, Semester und Leistung
* `deadlines.csv` – Fristen

Vor jedem Speichern wird die vorherige Version als `module.csv.bak` bzw. `deadlines.csv.bak` gesichert. Schlägt Laden oder Speichern fehl, zeigt die Anwendung einen Fehlerdialog.

## Kalender-Export (ICS)

Im Tab „Fristen“ exportiert „Als Kalender exportieren (.ics)“ alle Fristen als Ganztagestermine in die Datei `fristen.ics` im gewählten Ordner. Die Datei lässt sich in Outlook, Google Kalender oder Apple Kalender importieren.

## Tests

```bash
./mvnw -B verify   # 69 JUnit-Tests + JaCoCo-Report unter target/site/jacoco
```
