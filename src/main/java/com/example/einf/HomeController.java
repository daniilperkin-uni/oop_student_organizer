package com.example.einf;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

/**
 * FXML controller for the main view. After the split this class is a thin
 * coordinator: it keeps the {@code @FXML} injections for the single
 * home-view.fxml (which still binds one controller at the TabPane root) and
 * delegates every tab concern to its tab controller.
 *
 * <h2>Why a coordinator instead of fx:include per-tab controllers</h2>
 * The FXML binds one controller at the root; splitting via nested
 * {@code <fx:include>} would mean restructuring the FXML into separate files,
 * re-wiring all {@code fx:id} injections, and re-routing the shared save path.
 * On a JavaFX UI with no automated FXML-loading test coverage that is real
 * regression risk for zero behavior change. The programmatic split below
 * gives each tab its own focused class while the FXML stays untouched.
 */
public class HomeController {

    @FXML
    private FlowPane moduleGrid;

    @FXML
    private FlowPane deadlineGrid;

    @FXML
    private Label gpaLabel;

    @FXML
    private Label ectsLabel;

    @FXML
    private BarChart<String, Number> gradesChart;

    @FXML
    private TextField searchBar;

    @FXML
    private ComboBox<String> statusFilterBox;

    @FXML
    private ComboBox<String> semesterFilterBox;

    @FXML
    private ComboBox<String> sortFieldBox;

    @FXML
    private Button sortDirectionButton;

    @FXML
    private Button deadlineSortDirectionButton;

    private final ModuleTabController moduleTab = new ModuleTabController();
    private final DeadlineTabController deadlineTab = new DeadlineTabController();
    private final GradesTabController gradesTab = new GradesTabController();

    // Injected after FXMLLoader.setController() by Main; kept here so the
    // setters from before the split keep working unchanged.
    private ModulVerwaltung modulVerwaltung;
    private DeadlineManager deadlineManager;
    private SpeicherManager speicherManager;

    public void setModulVerwaltung(ModulVerwaltung modulVerwaltung) {
        this.modulVerwaltung = modulVerwaltung;
        moduleTab.setModulVerwaltung(modulVerwaltung);
        gradesTab.setModulVerwaltung(modulVerwaltung);
    }

    public void setDeadlineManager(DeadlineManager deadlineManager) {
        this.deadlineManager = deadlineManager;
        deadlineTab.setDeadlineManager(deadlineManager);
    }

    public void setSpeicherManager(SpeicherManager speicherManager) {
        this.speicherManager = speicherManager;
    }

    /**
     * Called by Main after all managers are wired. Hands the @FXML controls to
     * the tab controllers and initializes their listeners.
     */
    public void initializeControllers() {
        moduleTab.setModuleGrid(moduleGrid);
        moduleTab.setSearchBar(searchBar);
        moduleTab.setStatusFilterBox(statusFilterBox);
        moduleTab.setSemesterFilterBox(semesterFilterBox);
        moduleTab.setSortFieldBox(sortFieldBox);
        moduleTab.setSortDirectionButton(sortDirectionButton);

        deadlineTab.setDeadlineGrid(deadlineGrid);
        deadlineTab.setDeadlineSortDirectionButton(deadlineSortDirectionButton);

        gradesTab.setGpaLabel(gpaLabel);
        gradesTab.setEctsLabel(ectsLabel);
        gradesTab.setGradesChart(gradesChart);

        moduleTab.initializeSearch();
        moduleTab.initializeFilters();
        deadlineTab.initializeFilters();

        Runnable saveAll = this::speichereAenderungen;
        moduleTab.setOnSave(saveAll);
        deadlineTab.setOnSave(saveAll);

        // Re-apply the managers that were set before the FXML-injected controls
        // existed on the tab controllers, so grids render their current state.
        if (modulVerwaltung != null) {
            moduleTab.setModulVerwaltung(modulVerwaltung);
            gradesTab.setModulVerwaltung(modulVerwaltung);
        }
    }

    public void initializeSearch() {
        // Kept as a no-op-compatible entry point for Main; the actual search
        // listener lives in ModuleTabController and is attached in
        // initializeControllers().
    }

    public void initializeFilters() {
        // See initializeSearch(): listeners are attached in
        // initializeControllers(), which Main calls after both initialize*
        // methods.
    }

    @FXML
    public void clearSearchButtonOnAction(ActionEvent actionEvent) {
        moduleTab.clearSearch();
    }

    @FXML
    public void addModuleButtonOnAction(ActionEvent actionEvent) {
        moduleTab.addModuleButtonOnAction(actionEvent);
    }

    @FXML
    public void onDeadlinesTabSelected(Event event) {
        deadlineTab.onTabSelected();
    }

    @FXML
    public void addDeadlineButtonOnAction(ActionEvent actionEvent) {
        deadlineTab.addDeadlineButtonOnAction(actionEvent);
    }

    @FXML
    public void onGradesTabSelected(Event event) {
        gradesTab.onTabSelected();
    }

    private void speichereAenderungen() {
        if (speicherManager == null) return;
        // The ModulVerwaltung/DeadlineManager own private defensive copies of
        // the lists, so the SpeicherManager no longer shares their live state.
        // Push the managers' current state back into the SpeicherManager right
        // before persisting, so every add/edit/delete is written to disk.
        if (modulVerwaltung != null) {
            speicherManager.setModule(modulVerwaltung.getModule());
        }
        if (deadlineManager != null) {
            speicherManager.setDeadlines(deadlineManager.getDeadlines());
        }
        try {
            speicherManager.speichereDaten();
        } catch (java.io.IOException e) {
            UiDialogs.zeigeFehler("Speicherfehler", "Die Daten konnten nicht gespeichert werden.", e.getMessage());
        }
    }
}
