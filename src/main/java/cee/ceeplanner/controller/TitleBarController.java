package cee.ceeplanner.controller;


import cee.ceeplanner.ProjectManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;

public class TitleBarController {

    @FXML private HBox     customTitleBar;
    @FXML private MenuBar  menuBar;


    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        // ── Window drag ───────────────────────────────────────────────────────
        customTitleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        customTitleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) customTitleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // ── Hover-to-open menus ────────────────────────────────────────────────
        menuBar.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin == null) return;
            List<Node> menuButtons = menuBar.lookupAll(".menu-button").stream().toList();
            for (int i = 0; i < menuButtons.size(); i++) {
                Menu menu = menuBar.getMenus().get(i);
                Node btn  = menuButtons.get(i);
                btn.setOnMouseEntered(e -> {
                    menuBar.getMenus().forEach(Menu::hide);
                    menu.show();
                });
            }
        });
    }

    // ── File menu handlers ─────────────────────────────────────────────────────

    @FXML
    public void handleNew() {
        // Offer to save first if a project is open
        if (ProjectManager.get().hasProject()) {
            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.CONFIRMATION,
                    "Save current project before creating a new one?",
                    javafx.scene.control.ButtonType.YES,
                    javafx.scene.control.ButtonType.NO,
                    javafx.scene.control.ButtonType.CANCEL);
            confirm.setTitle("New Workspace");
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == javafx.scene.control.ButtonType.CANCEL) return;
                if (btn == javafx.scene.control.ButtonType.YES)    ProjectManager.get().save();
                ProjectManager.get().newProject();
            });
        } else {
            ProjectManager.get().newProject();
        }
    }

    @FXML
    public void handleOpen() {
        Stage stage = (Stage) customTitleBar.getScene().getWindow();
        boolean opened = ProjectManager.get().openProject(stage);
        if (opened) {
            showInfo("Opened project: " + ProjectManager.get().getCurrentProjectName());
        }
    }

    @FXML
    public void handleSave() {
        // If no project has been saved yet, fall through to Save As
        if (!ProjectManager.get().hasProject()) {
            handleSaveAs();
            return;
        }
        boolean ok = ProjectManager.get().save();
        if (ok) showInfo("Project saved.");
    }

    @FXML
    public void handleSaveAs() {
        Stage stage = (Stage) customTitleBar.getScene().getWindow();
        boolean ok  = ProjectManager.get().saveAs(stage);
        if (ok) showInfo("Saved as: " + ProjectManager.get().getCurrentProjectName());
    }

    // ── Window controls ────────────────────────────────────────────────────────

    @FXML
    public void handleMinimize() {
        Stage stage = (Stage) customTitleBar.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    public void handleExit() {
        Platform.exit();
        System.exit(0);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private void showInfo(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION, msg,
                javafx.scene.control.ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}