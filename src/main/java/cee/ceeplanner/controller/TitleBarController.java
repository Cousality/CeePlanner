package cee.ceeplanner.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.util.List;

public class TitleBarController {

    @FXML private HBox customTitleBar;
    @FXML private MenuBar menuBar;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        // Drag to move window
        customTitleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        customTitleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) customTitleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // Hover to switch between open menus
        menuBar.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin == null) return;

            List<Node> menuButtons = menuBar.lookupAll(".menu-button")
                    .stream().toList();

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