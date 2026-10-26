package com.example.einf;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

	private final SpeicherManager speicherManager;

	// Wired in start(); kept so stop() can flush through the controller's
	// synced save path (see HomeController.speichereAenderungen()).
	private HomeController controller;

	/**
	 * Aktion, die beim Beenden den aktuellen Stand persistiert. Wird in
	 * start() auf den Controller gesetzt; als Feld, damit der Beenden-Pfad
	 * ohne JavaFX-Toolkit testbar ist.
	 */
	private Runnable beendenAktion;

	/**
	 * true nur, wenn {@link SpeicherManager#ladeDaten()} beim Start ohne
	 * Ausnahme durchlief. Nach einem Ladefehler hält die Anwendung nur einen
	 * leeren Zustand - der darf beim Beenden nicht über die echten Dateien
	 * geschrieben werden.
	 */
	private boolean datenGeladen;

	/** Beim Laden aufgetretene Ausnahme, für den Hinweisdialog in {@link #start(Stage)}. */
	private IOException ladeFehler;

	/** JavaFX erzeugt die Anwendung über diesen Konstruktor. */
	public Main() {
		this(new SpeicherManager());
	}

	/** Nur für Tests: erlaubt einen Speicherort im Temp-Verzeichnis. */
	Main(SpeicherManager speicherManager) {
		this.speicherManager = speicherManager;
	}

	@Override
	public void start(Stage stage) throws IOException {
		if (!ladeDatenBeimStart()) {
			UiDialogs.zeigeFehler("Fehler", "Daten konnten nicht geladen werden",
					ladeFehler.getMessage() + "\nDie Anwendung startet mit leeren Daten.");
		}

		FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/example/einf/views/home-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load());
		scene.getStylesheets().add(
				Objects.requireNonNull(Main.class.getResource("/com/example/einf/styles/app.css")).toExternalForm());

		controller = fxmlLoader.getController();
		beendenAktion = controller::speichereAenderungen;

		ModulVerwaltung modulVerwaltung = new ModulVerwaltung(speicherManager.getModule());
		controller.setModulVerwaltung(modulVerwaltung);

		DeadlineManager deadlineManager = new DeadlineManager(speicherManager.getDeadlines());
		controller.setDeadlineManager(deadlineManager);

		controller.setSpeicherManager(speicherManager);
		controller.initializeControllers();

		stage.setTitle("Studentischer Organisationshelfer");
		stage.setScene(scene);
		stage.setMinWidth(800);
		stage.setMinHeight(600);
		stage.show();
	}

	@Override
	public void stop() {
		speichereBeimBeenden();
	}

	/**
	 * Lädt den gespeicherten Stand und merkt sich, ob das vollständig geklappt
	 * hat. Package-private statt inline in {@link #start(Stage)}, damit der
	 * Fehlerfall ohne JavaFX-Toolkit testbar ist.
	 *
	 * @return true, wenn alle Daten ohne Ausnahme geladen wurden
	 */
	boolean ladeDatenBeimStart() {
		try {
			speicherManager.ladeDaten();
			datenGeladen = true;
			ladeFehler = null;
		} catch (IOException e) {
			datenGeladen = false;
			ladeFehler = e;
		}
		return datenGeladen;
	}

	/**
	 * Persistiert den Stand beim Beenden - aber nur, wenn das Laden beim Start
	 * vollständig geklappt hat. Sonst würde der leere In-Memory-Zustand die
	 * echten Dateien überschreiben: genau der Datenverlust, den der
	 * Fehlerdialog "startet mit leeren Daten" ankündigt.
	 *
	 * <p>Der Flush läuft weiterhin über den Controller-Pfad statt direkt über
	 * den SpeicherManager-Schnappschuss: die ModulVerwaltung und der
	 * DeadlineManager halten den Live-Zustand und müssen vorher in den Store
	 * geschoben werden.</p>
	 */
	void speichereBeimBeenden() {
		if (!datenGeladen) {
			System.err.println(
					"Kein Speichern beim Beenden: Die Daten konnten beim Start nicht geladen werden.");
			return;
		}
		if (beendenAktion != null) {
			beendenAktion.run();
		}
	}

	/** Nur für Tests: ersetzt die Aktion, die beim Beenden speichert. */
	void setBeendenAktion(Runnable beendenAktion) {
		this.beendenAktion = beendenAktion;
	}

	public SpeicherManager getSpeicherManager() {
		return speicherManager;
	}
}
