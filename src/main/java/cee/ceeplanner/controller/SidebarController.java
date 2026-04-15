package cee.ceeplanner.controller;

import cee.ceeplanner.model.UmlClassNode;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class SidebarController {

    public static final String UML_CLASS_DRAG_KEY = "UML_CLASS_NODE";

    @FXML
    private StackPane templateContainer;

    @FXML
    public void initialize() {
        // 1. Create a real working node to sit in the toolbox
        UmlClassNode templateNode = new UmlClassNode();
        templateNode.setTemplateMode(true); // Disable canvas dragging & hide close button

        // 2. Setup standard Drag-and-Drop
        templateNode.setOnDragDetected(event -> {
            Dragboard db = templateNode.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();

            // Pass the exact mouse click offset so it drops centered on the cursor
            String payload = UML_CLASS_DRAG_KEY + "|" + event.getX() + "|" + event.getY();
            content.putString(payload);
            db.setContent(content);

            // Create a transparent snapshot to act as the ghost image while dragging
            SnapshotParameters snapshotParams = new SnapshotParameters();
            snapshotParams.setFill(Color.TRANSPARENT);
            db.setDragView(templateNode.snapshot(snapshotParams, null), event.getX(), event.getY());

            event.consume();
        });

        // 3. Add it to the sidebar UI
        templateContainer.getChildren().add(templateNode);
    }
}