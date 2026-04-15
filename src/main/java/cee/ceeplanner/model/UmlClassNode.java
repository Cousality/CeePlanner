package cee.ceeplanner.model;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * An interactive UML class-diagram node that lives on the workspace canvas.
 *
 * Features
 * ─────────
 * • Drag to reposition anywhere on the canvas.
 * • Double-click the class name to rename it inline.
 * • "＋ field" / "＋ method" buttons add new editable rows.
 * • Double-click any row label to edit it inline.
 * • Each row has a "−" button to delete it.
 * • "✕" button in the title bar removes the node from the canvas.
 */
public class UmlClassNode extends VBox {

    // ── Drag-to-reposition state ───────────────────────────────────────────────
    private double dragAnchorX;
    private double dragAnchorY;

    // ── Live label for the class name (kept so we can read/write it) ───────────
    private final Label classNameLabel = new Label("ClassName");

    // ── Containers for the two member sections ─────────────────────────────────
    private final VBox fieldsBox   = new VBox(3);
    private final VBox methodsBox  = new VBox(3);

    // ─────────────────────────────────────────────────────────────────────────
    public UmlClassNode() {
        buildStructure();
        setupCanvasDrag();

        // Seed with one field and one method so the node isn't empty on drop
        addMemberRow(fieldsBox,  "- field : Type");
        addMemberRow(methodsBox, "+ method() : void");
    }

    // ── UI construction ───────────────────────────────────────────────────────

    private void buildStructure() {
        setStyle(
                "-fx-border-color: #3a3a3a;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-background-color: white;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 6, 0, 2, 2);"
        );
        setMinWidth(180);
        setMaxWidth(300);

        getChildren().addAll(
                buildNameBar(),
                buildSectionBox(fieldsBox,  "＋ field"),
                new Separator(),
                buildSectionBox(methodsBox, "＋ method")
        );
    }

    /** Blue header containing the (editable) class name and a close button. */
    private HBox buildNameBar() {
        HBox bar = new HBox(6);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle(
                "-fx-background-color: #4A90D9;" +
                        "-fx-padding: 5 8 5 8;"
        );

        classNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 13;");
        classNameLabel.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                startEditingLabel(classNameLabel, bar, true);
                e.consume();
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: rgba(255,255,255,0.8);" +
                        "-fx-font-size: 11;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0 2 0 2;"
        );
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0 2 0 2;"
        ));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: rgba(255,255,255,0.8);" +
                        "-fx-font-size: 11;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0 2 0 2;"
        ));
        closeBtn.setOnAction(e -> removeFromCanvas());

        bar.getChildren().addAll(classNameLabel, spacer, closeBtn);
        return bar;
    }

    /**
     * Wraps a member VBox (fields or methods) together with an
     * "add row" button at the bottom.
     */
    private VBox buildSectionBox(VBox memberBox, String addButtonLabel) {
        memberBox.setStyle("-fx-padding: 4 8 2 8;");

        Button addBtn = new Button(addButtonLabel);
        addBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #888;" +
                        "-fx-font-size: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 1 0 3 0;"
        );
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #4A90D9;" +
                        "-fx-font-size: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 1 0 3 0;"
        ));
        addBtn.setOnMouseReleased(e -> addBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #888;" +
                        "-fx-font-size: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 1 0 3 0;"
        ));

        boolean isFields = (memberBox == fieldsBox);
        addBtn.setOnAction(e -> addMemberRow(
                memberBox,
                isFields ? "- newField : Type" : "+ newMethod() : void"
        ));

        VBox wrapper = new VBox(0, memberBox, addBtn);
        wrapper.setStyle("-fx-padding: 0 0 2 0;");
        return wrapper;
    }

    // ── Member row helpers ────────────────────────────────────────────────────

    /** Adds an editable member row (field or method) to the given section. */
    private void addMemberRow(VBox container, String defaultText) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 1 0 1 0;");

        Label memberLabel = new Label(defaultText);
        memberLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #222;");
        memberLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(memberLabel, Priority.ALWAYS);

        // Double-click to rename
        memberLabel.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                startEditingLabel(memberLabel, row, false);
                e.consume();
            }
        });

        Button removeBtn = new Button("−");
        removeBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #bbb;" +
                        "-fx-font-size: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0 2 0 2;"
        );
        removeBtn.setOnMouseEntered(e -> removeBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #e55;" +
                        "-fx-font-size: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0 2 0 2;"
        ));
        removeBtn.setOnMouseExited(e -> removeBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #bbb;" +
                        "-fx-font-size: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0 2 0 2;"
        ));
        removeBtn.setOnAction(e -> container.getChildren().remove(row));

        row.getChildren().addAll(memberLabel, removeBtn);
        container.getChildren().add(row);
    }

    // ── Inline editing ────────────────────────────────────────────────────────

    /**
     * Swaps a Label for a TextField, commits on Enter or focus-lost,
     * and swaps back.
     *
     * @param label         the Label to make editable
     * @param parent        the HBox/VBox that contains it
     * @param isClassName   true → white text style (used for the name bar)
     */
    private void startEditingLabel(Label label, Pane parent, boolean isClassName) {
        TextField tf = new TextField(label.getText());
        tf.setStyle(isClassName
                ? "-fx-font-weight: bold; -fx-font-size: 13; -fx-padding: 0; " +
                "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-border-color: white; -fx-border-radius: 2;"
                : "-fx-font-size: 11; -fx-padding: 0; -fx-border-color: #4A90D9; -fx-border-radius: 2;"
        );
        if (isClassName) HBox.setHgrow(tf, Priority.ALWAYS);
        else             HBox.setHgrow(tf, Priority.ALWAYS);

        int idx = parent.getChildren().indexOf(label);
        if (idx < 0) return;
        parent.getChildren().set(idx, tf);
        tf.requestFocus();
        tf.selectAll();

        Runnable commit = () -> {
            String text = tf.getText().trim();
            label.setText(text.isEmpty() ? label.getText() : text);
            int tfIdx = parent.getChildren().indexOf(tf);
            if (tfIdx >= 0) parent.getChildren().set(tfIdx, label);
        };

        tf.setOnAction(e -> commit.run());
        tf.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) commit.run();
        });
    }

    // ── Drag to reposition on canvas ─────────────────────────────────────────

    private void setupCanvasDrag() {
        // Record the cursor's scene-coordinate position when the user presses
        setOnMousePressed(event -> {
            // Do NOT consume so child controls (buttons, labels) still fire.
            // Buttons consume their own press, so they won't trigger a drag.
            dragAnchorX = event.getSceneX() - getLayoutX();
            dragAnchorY = event.getSceneY() - getLayoutY();
            toFront();
        });

        setOnMouseDragged(event -> {
            double newLeft = event.getSceneX() - dragAnchorX;
            double newTop  = event.getSceneY() - dragAnchorY;

            // Clamp to canvas bounds if possible
            if (getParent() instanceof AnchorPane ap) {
                newLeft = Math.max(0, Math.min(newLeft, ap.getWidth()  - getWidth()));
                newTop  = Math.max(0, Math.min(newTop,  ap.getHeight() - getHeight()));
            }

            AnchorPane.setLeftAnchor(this, newLeft);
            AnchorPane.setTopAnchor(this, newTop);
        });
    }

    // ── Remove from canvas ────────────────────────────────────────────────────

    private void removeFromCanvas() {
        if (getParent() instanceof Pane parent) {
            parent.getChildren().remove(this);
        }
    }

    // ── Template Mode (For Sidebar Use) ───────────────────────────────────────
    public void setTemplateMode(boolean isTemplate) {
        if (isTemplate) {

            // Disable the canvas-specific repositioning logic
            setOnMousePressed(null);
            setOnMouseDragged(null);
        }
    }
}