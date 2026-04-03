package cee.ceeplanner.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class TitleBarController {
    @FXML
    private HBox customTitleBar;

    // Variables to track mouse coordinates for dragging
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        // 1. Grab the window coordinates when the user clicks the header
        customTitleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        // 2. Move the entire window as the user drags their mouse
        customTitleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) customTitleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

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
}
