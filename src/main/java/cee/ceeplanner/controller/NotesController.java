package cee.ceeplanner.controller;

import cee.ceeplanner.ProjectManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Notes tab controller.
 *
 * Persistence is now handled by ProjectManager (File > Save / Save As).
 * The old flat notes.txt approach has been removed.
 *
 * Serialisation format (notes.json):
 * {
 *   "content": "the full notes text here"
 * }
 */
public class NotesController {

    @FXML private TextArea notesTextArea;
    @FXML private Label    statusLabel;

    private String lastSavedContent = "";

    @FXML
    public void initialize() {
        ProjectManager.get().registerNotes(this);

        // Track unsaved changes
        notesTextArea.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.equals(lastSavedContent)) {
                statusLabel.setText("Unsaved changes");
            } else {
                statusLabel.setText("");
            }
        });
    }

    // ── Public API (used by ProjectManager) ────────────────────────────────────

    /** Serialises notes content to a JSON string. */
    public String toJson() {
        String content = notesTextArea.getText();
        String escaped = content
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        String json = "{\n  \"content\": \"" + escaped + "\"\n}";

        // Mark as saved
        lastSavedContent = content;
        statusLabel.setText("Saved \u2713");
        return json;
    }

    /** Restores notes content from a JSON string produced by {@link #toJson()}. */
    public void fromJson(String json) {
        // Simple extraction: find value of "content" key
        String key = "\"content\"";
        int ki = json.indexOf(key);
        if (ki < 0) {
            notesTextArea.clear();
            return;
        }
        int colon = json.indexOf(':', ki + key.length());
        if (colon < 0) return;
        int q1 = json.indexOf('"', colon + 1);
        if (q1 < 0) return;

        StringBuilder sb = new StringBuilder();
        for (int i = q1 + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(++i);
                switch (next) {
                    case 'n'  -> sb.append('\n');
                    case 'r'  -> sb.append('\r');
                    case 't'  -> sb.append('\t');
                    default   -> sb.append(next);
                }
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }

        String content = sb.toString();
        notesTextArea.setText(content);
        lastSavedContent = content;
        statusLabel.setText("");
    }

    // ── Accessors (kept for any future inter-controller use) ───────────────────

    public String getNotesContent() { return notesTextArea.getText(); }

    public void setNotesContent(String content) {
        notesTextArea.setText(content);
        lastSavedContent = content;
        statusLabel.setText("");
    }

    // ── Clear button ───────────────────────────────────────────────────────────

    @FXML
    private void onClearNotes() {
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION,
                "Clear all notes? This cannot be undone.",
                javafx.scene.control.ButtonType.YES,
                javafx.scene.control.ButtonType.CANCEL);
        confirm.setTitle("Clear Notes");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == javafx.scene.control.ButtonType.YES) {
                notesTextArea.clear();
                statusLabel.setText("");
            }
        });
    }
}