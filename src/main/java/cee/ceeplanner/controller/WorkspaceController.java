package cee.ceeplanner.controller;

import cee.ceeplanner.model.UmlClassNode;
import javafx.fxml.FXML;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;

public class WorkspaceController {

    @FXML private AnchorPane canvas;

    // ── Drag-over: accept the transfer so the cursor shows a "copy" icon ──────

    @FXML
    private void onCanvasDragOver(DragEvent event) {
        if (event.getDragboard().hasString()) {
            String payload = event.getDragboard().getString();
            if (payload != null && payload.startsWith(SidebarController.UML_CLASS_DRAG_KEY)) {
                event.acceptTransferModes(TransferMode.COPY);
            }
        }
        event.consume();
    }

    // ── Drag-dropped: create a new UML node at the cursor position ─────────────

    @FXML
    private void onCanvasDragDropped(DragEvent event) {
        boolean success = false;

        String payload = event.getDragboard().getString();
        if (payload != null && payload.startsWith(SidebarController.UML_CLASS_DRAG_KEY)) {

            // Parse the optional press-offset encoded in the payload
            double offsetX = 75;   // default: half of the node's ~150 px width
            double offsetY = 30;   // default: ~top quarter of the node

            String[] parts = payload.split("\\|");
            if (parts.length == 3) {
                try {
                    offsetX = Double.parseDouble(parts[1]);
                    offsetY = Double.parseDouble(parts[2]);
                } catch (NumberFormatException ignored) { /* use defaults */ }
            }

            // Create the interactive UML class node
            UmlClassNode node = new UmlClassNode();

            // Position so the node's grabbed point lands under the cursor
            double left = Math.max(0, event.getX() - offsetX);
            double top  = Math.max(0, event.getY() - offsetY);

            AnchorPane.setLeftAnchor(node, left);
            AnchorPane.setTopAnchor(node, top);

            canvas.getChildren().add(node);
            success = true;
        }

        event.setDropCompleted(success);
        event.consume();
    }
}