package cee.ceeplanner;

import cee.ceeplanner.controller.NotesController;
import cee.ceeplanner.controller.TodoController;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Singleton that owns the current project lifecycle.
 *
 * Project structure on disk:
 *   projects/
 *     <project-name>/
 *       todo.json
 *       notes.json
 *
 * Dependencies (add to pom.xml / build.gradle):
 *   com.fasterxml.jackson.core : jackson-databind : 2.17.x
 */
public class ProjectManager {

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static final ProjectManager INSTANCE = new ProjectManager();
    public static ProjectManager get() { return INSTANCE; }
    private ProjectManager() {}

    // ── State ─────────────────────────────────────────────────────────────────
    private Path currentProjectPath = null;   // e.g. projects/MyProject
    private String currentProjectName = null;

    // Controllers are registered when their FXML loads
    private TodoController todoController;
    private NotesController notesController;

    // ── Controller registration ────────────────────────────────────────────────
    public void registerTodo(TodoController c)   { this.todoController  = c; }
    public void registerNotes(NotesController c) { this.notesController = c; }

    // ── Project name / path helpers ───────────────────────────────────────────
    public String getCurrentProjectName() { return currentProjectName; }
    public boolean hasProject()           { return currentProjectPath != null; }

    // ── New project ───────────────────────────────────────────────────────────
    /**
     * Clears both controllers and resets the current-project state.
     * Does NOT prompt for a name — call from "New Workspace".
     */
    public void newProject() {
        currentProjectPath = null;
        currentProjectName = null;
        if (todoController  != null) todoController.clearBoard();
        if (notesController != null) notesController.setNotesContent("");
    }

    // ── Save ──────────────────────────────────────────────────────────────────
    /**
     * Saves to the current project folder.
     * Returns false if no project has been set (caller should trigger Save As).
     */
    public boolean save() {
        if (currentProjectPath == null) return false;
        return writeFiles(currentProjectPath);
    }

    /**
     * Prompts for a project name, creates the folder, and saves.
     * Accepts an optional stage for dialog parenting (may be null).
     */
    public boolean saveAs(Stage owner) {
        // Ask for a project name via a simple TextInputDialog
        javafx.scene.control.TextInputDialog dialog =
                new javafx.scene.control.TextInputDialog(
                        currentProjectName != null ? currentProjectName : "MyProject");
        dialog.setTitle("Save Project As");
        dialog.setHeaderText(null);
        dialog.setContentText("Project name:");
        if (owner != null) dialog.initOwner(owner);

        java.util.Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return false;

        String name = result.get().trim();
        if (name.isEmpty()) return false;

        Path projectsRoot = Paths.get("projects");
        Path folder       = projectsRoot.resolve(sanitise(name));

        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            showError("Could not create project folder:\n" + e.getMessage());
            return false;
        }

        currentProjectPath = folder;
        currentProjectName = name;

        return writeFiles(folder);
    }

    // ── Open ──────────────────────────────────────────────────────────────────
    /**
     * Opens a directory chooser inside the projects/ folder.
     * Returns false if the user cancels or the folder is invalid.
     */
    public boolean openProject(Stage owner) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Open Project");

        // Default to projects/ if it exists
        File projectsRoot = Paths.get("projects").toFile();
        if (projectsRoot.exists()) chooser.setInitialDirectory(projectsRoot);

        File selected = chooser.showDialog(owner);
        if (selected == null) return false;

        Path folder = selected.toPath();
        if (!Files.exists(folder.resolve("todo.json")) &&
                !Files.exists(folder.resolve("notes.json"))) {
            showError("The selected folder does not contain a valid CeePlanner project.");
            return false;
        }

        currentProjectPath = folder;
        currentProjectName = folder.getFileName().toString();

        return readFiles(folder);
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private boolean writeFiles(Path folder) {
        try {
            if (todoController != null) {
                String todoJson = todoController.toJson();
                Files.writeString(folder.resolve("todo.json"), todoJson);
            }
            if (notesController != null) {
                String notesJson = notesController.toJson();
                Files.writeString(folder.resolve("notes.json"), notesJson);
            }
            return true;
        } catch (IOException e) {
            showError("Save failed:\n" + e.getMessage());
            return false;
        }
    }

    private boolean readFiles(Path folder) {
        try {
            Path todoPath  = folder.resolve("todo.json");
            Path notesPath = folder.resolve("notes.json");

            if (Files.exists(todoPath) && todoController != null) {
                todoController.fromJson(Files.readString(todoPath));
            }
            if (Files.exists(notesPath) && notesController != null) {
                notesController.fromJson(Files.readString(notesPath));
            }
            return true;
        } catch (IOException e) {
            showError("Open failed:\n" + e.getMessage());
            return false;
        }
    }

    /** Strips characters that are invalid in folder names. */
    private static String sanitise(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private static void showError(String msg) {
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR, msg,
                        javafx.scene.control.ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}