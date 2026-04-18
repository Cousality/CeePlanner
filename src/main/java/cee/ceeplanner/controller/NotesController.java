package cee.ceeplanner.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class NotesController {

    @FXML private TextArea notesTextArea;
    @FXML private Label statusLabel;

    private String lastSavedContent = "";

    @FXML
    public void initialize() {
        // Track unsaved changes to show feedback in the status label
        notesTextArea.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.equals(lastSavedContent)) {
                statusLabel.setText("Unsaved changes");
            } else {
                statusLabel.setText("");
            }
        });
    }

    /**
     * Called by TitleBarController (File > Save) to persist notes.
     * Returns true on success so the caller can react if needed.
     */
    public boolean saveNotes() {
        try {
            java.nio.file.Files.writeString(
                    java.nio.file.Path.of("notes.txt"),
                    notesTextArea.getText());
            lastSavedContent = notesTextArea.getText();
            statusLabel.setText("Saved ✓");
            return true;
        } catch (java.io.IOException e) {
            statusLabel.setText("Save failed!");
            e.printStackTrace();
            return false;
        }
    }

    /** Loads previously saved notes from disk on startup. */
    public void loadNotes() {
        java.nio.file.Path path = java.nio.file.Path.of("notes.txt");
        if (java.nio.file.Files.exists(path)) {
            try {
                String content = java.nio.file.Files.readString(path);
                notesTextArea.setText(content);
                lastSavedContent = content;
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Clears all notes after a confirmation dialog. */
    @FXML
    private void onClearNotes() {
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION,
                "Clear all notes? This cannot be undone.",
                javafx.scene.control.ButtonType.YES,
                javafx.scene.control.ButtonType.CANCEL
        );
        confirm.setTitle("Clear Notes");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == javafx.scene.control.ButtonType.YES) {
                notesTextArea.clear();
                statusLabel.setText("");
            }
        });
    }

    public String getNotesContent() { return notesTextArea.getText(); }
    public void setNotesContent(String content) {
        notesTextArea.setText(content);
        lastSavedContent = content;
        statusLabel.setText("");
    }
}