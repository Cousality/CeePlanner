package cee.ceeplanner.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class SidebarController {

    @FXML
    private StackPane sidebarContainer;

    @FXML
    private void toggleSidebar() {
        // Get the current state
        boolean isVisible = sidebarContainer.isVisible();

        // Toggle visibility
        sidebarContainer.setVisible(!isVisible);

        // Toggle 'managed' so the center area expands to fill the gap
        sidebarContainer.setManaged(!isVisible);
    }
}