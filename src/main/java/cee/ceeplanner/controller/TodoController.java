package cee.ceeplanner.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.ResourceBundle;

public class TodoController implements Initializable {

    @FXML private HBox listsContainer;
    @FXML private ScrollPane boardScroll;

    // ID used to locate the "Add a list" button box among column children
    private static final String ADD_LIST_BOX_ID = "add-list-btn-box";

    // ─────────────────────────────────────────────────────────────────────────
    //  Initialisation
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        appendAddListButton();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Add-List button / form
    // ─────────────────────────────────────────────────────────────────────────

    /** Appends the standing "+ Add a list" button at the right end of the board. */
    private void appendAddListButton() {
        VBox box = new VBox();
        box.setId(ADD_LIST_BOX_ID);
        box.getStyleClass().add("todo-add-list-box");
        box.setAlignment(Pos.TOP_CENTER);

        Button btn = new Button("+ Add a list");
        btn.getStyleClass().add("todo-add-list-btn");
        btn.setOnAction(e -> showAddListForm(box));

        box.getChildren().add(btn);
        listsContainer.getChildren().add(box);
    }

    /** Replaces the button with an inline title-entry form. */
    private void showAddListForm(VBox box) {
        box.getChildren().clear();

        TextField field = new TextField();
        field.setPromptText("List title…");
        field.getStyleClass().add("todo-input-field");

        Button confirm = new Button("Add list");
        confirm.getStyleClass().add("todo-confirm-btn");

        Button cancel = makeXButton();

        HBox row = new HBox(6, confirm, cancel);
        row.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(field, row);
        field.requestFocus();

        Runnable submit = () -> {
            String title = field.getText().trim();
            if (!title.isEmpty()) createList(title);
        };

        confirm.setOnAction(e -> submit.run());
        cancel.setOnAction(e -> resetAddListBox(box));
        field.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)  submit.run();
            if (e.getCode() == KeyCode.ESCAPE) resetAddListBox(box);
        });
    }

    /** Restores the "+ Add a list" button inside its box. */
    private void resetAddListBox(VBox box) {
        box.getChildren().clear();
        Button btn = new Button("+ Add a list");
        btn.getStyleClass().add("todo-add-list-btn");
        btn.setOnAction(e -> showAddListForm(box));
        box.getChildren().add(btn);
    }

    /** Finds the add-list box in the container. */
    private VBox getAddListBox() {
        return listsContainer.getChildren().stream()
                .filter(n -> ADD_LIST_BOX_ID.equals(n.getId()))
                .map(n -> (VBox) n)
                .findFirst()
                .orElse(null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  List column creation
    // ─────────────────────────────────────────────────────────────────────────

    private void createList(String title) {
        VBox addListBox = getAddListBox();
        int insertAt = addListBox != null
                ? listsContainer.getChildren().indexOf(addListBox)
                : listsContainer.getChildren().size();

        listsContainer.getChildren().add(insertAt, buildListColumn(title));

        if (addListBox != null) resetAddListBox(addListBox);

        // Scroll the board all the way right so the user sees the new list
        boardScroll.layout();
        boardScroll.setHvalue(1.0);
    }

    private VBox buildListColumn(String title) {
        // ── Outer column ────────────────────────────────────────────────────
        VBox column = new VBox(8);
        column.getStyleClass().add("todo-list-column");
        column.setPrefWidth(264);
        column.setMaxWidth(264);

        // ── Header ───────────────────────────────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("todo-list-header");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("todo-list-title");
        titleLabel.setWrapText(false);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Inline rename: double-click the title
        titleLabel.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) startRenameList(header, titleLabel, column);
        });

        Button deleteListBtn = makeXButton();
        deleteListBtn.getStyleClass().add("todo-list-delete-btn");
        deleteListBtn.setOnAction(e -> {
            listsContainer.getChildren().remove(column);
        });

        header.getChildren().addAll(titleLabel, deleteListBtn);

        // ── Tasks area ───────────────────────────────────────────────────────
        // Wrap in a ScrollPane so a list with many cards stays contained
        VBox tasksBox = new VBox(6);
        tasksBox.getStyleClass().add("todo-tasks-box");

        ScrollPane tasksScroll = new ScrollPane(tasksBox);
        tasksScroll.getStyleClass().add("todo-tasks-scroll");
        tasksScroll.setFitToWidth(true);
        tasksScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tasksScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        tasksScroll.setMaxHeight(420);
        VBox.setVgrow(tasksScroll, Priority.ALWAYS);

        // ── Add-card area ────────────────────────────────────────────────────
        VBox addCardArea = new VBox(6);
        addCardArea.getStyleClass().add("todo-add-card-area");
        resetAddCardArea(addCardArea, tasksBox);

        column.getChildren().addAll(header, tasksScroll, addCardArea);
        return column;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  List rename
    // ─────────────────────────────────────────────────────────────────────────

    private void startRenameList(HBox header, Label titleLabel, VBox column) {
        header.getChildren().clear();

        TextField field = new TextField(titleLabel.getText());
        field.getStyleClass().add("todo-input-field");
        field.getStyleClass().add("todo-rename-field");
        HBox.setHgrow(field, Priority.ALWAYS);

        header.getChildren().add(field);
        field.selectAll();
        field.requestFocus();

        Runnable commit = () -> {
            String newTitle = field.getText().trim();
            if (!newTitle.isEmpty()) titleLabel.setText(newTitle);
            finishRenameList(header, titleLabel, column);
        };

        field.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)  commit.run();
            if (e.getCode() == KeyCode.ESCAPE) finishRenameList(header, titleLabel, column);
        });
        field.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) commit.run();
        });
    }

    private void finishRenameList(HBox header, Label titleLabel, VBox column) {
        header.getChildren().clear();
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Button deleteListBtn = makeXButton();
        deleteListBtn.getStyleClass().add("todo-list-delete-btn");
        deleteListBtn.setOnAction(e -> {
             listsContainer.getChildren().remove(column);
        });

        header.getChildren().addAll(titleLabel, deleteListBtn);

        titleLabel.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2) startRenameList(header, titleLabel, column);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Add-card area
    // ─────────────────────────────────────────────────────────────────────────

    /** Shows the "+ Add a card" button in the given area. */
    private void resetAddCardArea(VBox area, VBox tasksBox) {
        area.getChildren().clear();
        Button btn = new Button("+ Add a card");
        btn.getStyleClass().add("todo-add-card-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> showAddCardForm(area, tasksBox));
        area.getChildren().add(btn);
    }

    /** Replaces the button with a textarea + confirm/cancel form. */
    private void showAddCardForm(VBox area, VBox tasksBox) {
        area.getChildren().clear();

        TextArea input = new TextArea();
        input.setPromptText("Enter a title for this card…");
        input.getStyleClass().add("todo-card-input");
        input.setPrefRowCount(3);
        input.setWrapText(true);

        Button confirm = new Button("Add card");
        confirm.getStyleClass().add("todo-confirm-btn");

        Button cancel = makeXButton();

        HBox row = new HBox(6, confirm, cancel);
        row.setAlignment(Pos.CENTER_LEFT);

        area.getChildren().addAll(input, row);
        input.requestFocus();

        confirm.setOnAction(e -> submitCard(input, area, tasksBox));
        cancel.setOnAction(e -> resetAddCardArea(area, tasksBox));
        input.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) resetAddCardArea(area, tasksBox);
        });
    }

    private void submitCard(TextArea input, VBox area, VBox tasksBox) {
        String text = input.getText().trim();
        if (!text.isEmpty()) {
            tasksBox.getChildren().add(buildTaskCard(tasksBox, text));
        }
        // Keep the form open (like Trello) but clear the text so the user can add more
        input.clear();
        input.requestFocus();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Task card
    // ─────────────────────────────────────────────────────────────────────────

    private HBox buildTaskCard(VBox tasksBox, String text) {
        HBox card = new HBox(8);
        card.getStyleClass().add("todo-task-card");
        card.setAlignment(Pos.TOP_LEFT);

        // Checkbox to mark done
        CheckBox check = new CheckBox();
        check.getStyleClass().add("todo-task-check");

        // Task label (wraps)
        Label label = new Label(text);
        label.getStyleClass().add("todo-task-label");
        label.setWrapText(true);
        HBox.setHgrow(label, Priority.ALWAYS);

        check.selectedProperty().addListener((obs, old, done) -> {
            if (done) label.getStyleClass().add("todo-task-label-done");
            else      label.getStyleClass().remove("todo-task-label-done");
        });

        // Delete card button — only visible on hover
        Button del = makeXButton();
        del.getStyleClass().add("todo-task-delete-btn");
        del.setVisible(false);
        del.setOnAction(e -> tasksBox.getChildren().remove(card));

        card.setOnMouseEntered(e -> del.setVisible(true));
        card.setOnMouseExited(e  -> del.setVisible(false));

        card.getChildren().addAll(check, label, del);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Shared helper
    // ─────────────────────────────────────────────────────────────────────────

    /** Creates a small ✕ icon button used in multiple places. */
    private Button makeXButton() {
        Button btn = new Button("✕");
        btn.getStyleClass().add("todo-x-btn");
        return btn;
    }
}