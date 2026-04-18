package cee.ceeplanner.controller;

import cee.ceeplanner.ProjectManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Trello-style board tab.
 *
 * Serialisation format (todo.json):
 * {
 *   "lists": [
 *     {
 *       "title": "Backlog",
 *       "cards": [
 *         { "text": "Do the thing", "done": false }
 *       ]
 *     }
 *   ]
 * }
 *
 * Written with a hand-rolled serialiser so no external JSON library is needed.
 */
public class TodoController implements Initializable {

    @FXML private HBox       listsContainer;
    @FXML private ScrollPane boardScroll;

    private static final String ADD_LIST_BOX_ID = "add-list-btn-box";

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ProjectManager.get().registerTodo(this);
        appendAddListButton();
    }

    // ── Public API (used by ProjectManager) ────────────────────────────────────

    /** Removes all lists and resets the board to an empty state. */
    public void clearBoard() {
        listsContainer.getChildren().clear();
        appendAddListButton();
    }

    // ── Serialisation ─────────────────────────────────────────────────────────

    /** Serialises the current board to a JSON string. */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"lists\": [\n");

        List<VBox> columns = getListColumns();
        for (int ci = 0; ci < columns.size(); ci++) {
            VBox col = columns.get(ci);
            String title = getColumnTitle(col);
            List<CardData> cards = getCardData(col);

            sb.append("    {\n");
            sb.append("      \"title\": ").append(jsonStr(title)).append(",\n");
            sb.append("      \"cards\": [\n");
            for (int ki = 0; ki < cards.size(); ki++) {
                CardData c = cards.get(ki);
                sb.append("        { \"text\": ").append(jsonStr(c.text))
                        .append(", \"done\": ").append(c.done).append(" }");
                if (ki < cards.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("      ]\n");
            sb.append("    }");
            if (ci < columns.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("  ]\n}");
        return sb.toString();
    }

    /** Rebuilds the board from a JSON string produced by {@link #toJson()}. */
    public void fromJson(String json) {
        clearBoard();
        try {
            String listsBlock = between(json, "\"lists\"", "[", "]");
            if (listsBlock == null) return;

            List<String> listObjects = splitJsonObjects(listsBlock);
            for (String listObj : listObjects) {
                String title = stringField(listObj, "title");
                if (title == null) continue;

                String cardsBlock = between(listObj, "\"cards\"", "[", "]");
                List<CardData> cards = new ArrayList<>();
                if (cardsBlock != null) {
                    for (String cardObj : splitJsonObjects(cardsBlock)) {
                        String text = stringField(cardObj, "text");
                        boolean done = boolField(cardObj, "done");
                        if (text != null) cards.add(new CardData(text, done));
                    }
                }

                VBox addListBox = getAddListBox();
                int insertAt = addListBox != null
                        ? listsContainer.getChildren().indexOf(addListBox)
                        : listsContainer.getChildren().size();
                listsContainer.getChildren().add(insertAt, buildListColumn(title, cards));
            }
        } catch (Exception e) {
            System.err.println("[TodoController] fromJson parse error: " + e.getMessage());
        }
    }

    // ── Add-List button / form ─────────────────────────────────────────────────

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

    private void showAddListForm(VBox box) {
        box.getChildren().clear();

        TextField field = new TextField();
        field.setPromptText("List title\u2026");
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

    private void resetAddListBox(VBox box) {
        box.getChildren().clear();
        Button btn = new Button("+ Add a list");
        btn.getStyleClass().add("todo-add-list-btn");
        btn.setOnAction(e -> showAddListForm(box));
        box.getChildren().add(btn);
    }

    private VBox getAddListBox() {
        return listsContainer.getChildren().stream()
                .filter(n -> ADD_LIST_BOX_ID.equals(n.getId()))
                .map(n -> (VBox) n)
                .findFirst().orElse(null);
    }

    // ── List column creation ───────────────────────────────────────────────────

    private void createList(String title) {
        VBox addListBox = getAddListBox();
        int insertAt = addListBox != null
                ? listsContainer.getChildren().indexOf(addListBox)
                : listsContainer.getChildren().size();

        listsContainer.getChildren().add(insertAt, buildListColumn(title, List.of()));

        if (addListBox != null) resetAddListBox(addListBox);

        boardScroll.layout();
        boardScroll.setHvalue(1.0);
    }

    private VBox buildListColumn(String title, List<CardData> initialCards) {
        VBox column = new VBox(8);
        column.getStyleClass().add("todo-list-column");
        column.setPrefWidth(264);
        column.setMaxWidth(264);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("todo-list-header");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("todo-list-title");
        titleLabel.setWrapText(false);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) startRenameList(header, titleLabel, column);
        });

        Button deleteListBtn = makeXButton();
        deleteListBtn.getStyleClass().add("todo-list-delete-btn");
        deleteListBtn.setOnAction(e -> listsContainer.getChildren().remove(column));
        header.getChildren().addAll(titleLabel, deleteListBtn);

        // Tasks
        VBox tasksBox = new VBox(6);
        tasksBox.getStyleClass().add("todo-tasks-box");

        for (CardData cd : initialCards) {
            tasksBox.getChildren().add(buildTaskCard(tasksBox, cd.text, cd.done));
        }

        ScrollPane tasksScroll = new ScrollPane(tasksBox);
        tasksScroll.getStyleClass().add("todo-tasks-scroll");
        tasksScroll.setFitToWidth(true);
        tasksScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tasksScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        tasksScroll.setMaxHeight(420);
        VBox.setVgrow(tasksScroll, Priority.ALWAYS);

        // Add-card area
        VBox addCardArea = new VBox(6);
        addCardArea.getStyleClass().add("todo-add-card-area");
        resetAddCardArea(addCardArea, tasksBox);

        column.getChildren().addAll(header, tasksScroll, addCardArea);
        return column;
    }

    // ── List rename ────────────────────────────────────────────────────────────

    private void startRenameList(HBox header, Label titleLabel, VBox column) {
        header.getChildren().clear();
        TextField field = new TextField(titleLabel.getText());
        field.getStyleClass().addAll("todo-input-field", "todo-rename-field");
        HBox.setHgrow(field, Priority.ALWAYS);
        header.getChildren().add(field);
        field.selectAll();
        field.requestFocus();

        Runnable commit = () -> {
            String t = field.getText().trim();
            if (!t.isEmpty()) titleLabel.setText(t);
            finishRenameList(header, titleLabel, column);
        };
        field.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)  commit.run();
            if (e.getCode() == KeyCode.ESCAPE) finishRenameList(header, titleLabel, column);
        });
        field.focusedProperty().addListener((obs, old, focused) -> { if (!focused) commit.run(); });
    }

    private void finishRenameList(HBox header, Label titleLabel, VBox column) {
        header.getChildren().clear();
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        Button del = makeXButton();
        del.getStyleClass().add("todo-list-delete-btn");
        del.setOnAction(e -> listsContainer.getChildren().remove(column));
        header.getChildren().addAll(titleLabel, del);
        titleLabel.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2) startRenameList(header, titleLabel, column);
        });
    }

    // ── Add-card area ──────────────────────────────────────────────────────────

    private void resetAddCardArea(VBox area, VBox tasksBox) {
        area.getChildren().clear();
        Button btn = new Button("+ Add a card");
        btn.getStyleClass().add("todo-add-card-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> showAddCardForm(area, tasksBox));
        area.getChildren().add(btn);
    }

    private void showAddCardForm(VBox area, VBox tasksBox) {
        area.getChildren().clear();
        TextArea input = new TextArea();
        input.setPromptText("Enter a title for this card\u2026");
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

        confirm.setOnAction(e -> submitCard(input, tasksBox));
        cancel.setOnAction(e -> resetAddCardArea(area, tasksBox));
        input.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) resetAddCardArea(area, tasksBox);
        });
    }

    private void submitCard(TextArea input, VBox tasksBox) {
        String text = input.getText().trim();
        if (!text.isEmpty()) {
            tasksBox.getChildren().add(buildTaskCard(tasksBox, text, false));
        }
        input.clear();
        input.requestFocus();
    }

    // ── Task card ──────────────────────────────────────────────────────────────

    private HBox buildTaskCard(VBox tasksBox, String text, boolean done) {
        HBox card = new HBox(8);
        card.getStyleClass().add("todo-task-card");
        card.setAlignment(Pos.TOP_LEFT);

        CheckBox check = new CheckBox();
        check.getStyleClass().add("todo-task-check");
        check.setSelected(done);

        Label label = new Label(text);
        label.getStyleClass().add("todo-task-label");
        label.setWrapText(true);
        HBox.setHgrow(label, Priority.ALWAYS);
        if (done) label.getStyleClass().add("todo-task-label-done");

        check.selectedProperty().addListener((obs, old, isDone) -> {
            if (isDone) label.getStyleClass().add("todo-task-label-done");
            else        label.getStyleClass().remove("todo-task-label-done");
        });

        Button del = makeXButton();
        del.getStyleClass().add("todo-task-delete-btn");
        del.setVisible(false);
        del.setOnAction(e -> tasksBox.getChildren().remove(card));

        card.setOnMouseEntered(e -> del.setVisible(true));
        card.setOnMouseExited(e  -> del.setVisible(false));

        card.getChildren().addAll(check, label, del);
        return card;
    }

    // ── Shared helper ──────────────────────────────────────────────────────────

    private Button makeXButton() {
        Button btn = new Button("\u2715");
        btn.getStyleClass().add("todo-x-btn");
        return btn;
    }

    // ── Serialisation helpers ──────────────────────────────────────────────────

    private List<VBox> getListColumns() {
        return listsContainer.getChildren().stream()
                .filter(n -> n instanceof VBox && !ADD_LIST_BOX_ID.equals(n.getId()))
                .map(n -> (VBox) n)
                .toList();
    }

    private String getColumnTitle(VBox col) {
        if (col.getChildren().isEmpty()) return "";
        javafx.scene.Node headerNode = col.getChildren().get(0);
        if (!(headerNode instanceof HBox header)) return "";
        return header.getChildren().stream()
                .filter(n -> n instanceof Label)
                .map(n -> ((Label) n).getText())
                .findFirst().orElse("");
    }

    private List<CardData> getCardData(VBox col) {
        return col.getChildren().stream()
                .filter(n -> n instanceof ScrollPane)
                .map(n -> (ScrollPane) n)
                .flatMap(sp -> {
                    if (!(sp.getContent() instanceof VBox tasksBox)) return java.util.stream.Stream.empty();
                    return tasksBox.getChildren().stream();
                })
                .filter(n -> n instanceof HBox)
                .map(n -> (HBox) n)
                .map(card -> {
                    String text = card.getChildren().stream()
                            .filter(n -> n instanceof Label)
                            .map(n -> ((Label) n).getText())
                            .findFirst().orElse("");
                    boolean d = card.getChildren().stream()
                            .filter(n -> n instanceof CheckBox)
                            .map(n -> ((CheckBox) n).isSelected())
                            .findFirst().orElse(false);
                    return new CardData(text, d);
                })
                .toList();
    }

    private static String jsonStr(String s) {
        return "\"" + s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

    private static String between(String json, String afterKey, String open, String close) {
        int keyIdx = json.indexOf(afterKey);
        if (keyIdx < 0) return null;
        int start = json.indexOf(open, keyIdx + afterKey.length());
        if (start < 0) return null;
        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (String.valueOf(c).equals(open))  depth++;
            if (String.valueOf(c).equals(close)) {
                if (--depth == 0) return json.substring(start + 1, i);
            }
        }
        return null;
    }

    private static List<String> splitJsonObjects(String arrayBody) {
        List<String> result = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < arrayBody.length(); i++) {
            char c = arrayBody.charAt(i);
            if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}') { if (--depth == 0 && start >= 0) result.add(arrayBody.substring(start, i + 1)); }
        }
        return result;
    }

    private static String stringField(String obj, String key) {
        String search = "\"" + key + "\"";
        int ki = obj.indexOf(search);
        if (ki < 0) return null;
        int colon = obj.indexOf(':', ki + search.length());
        if (colon < 0) return null;
        int q1 = obj.indexOf('"', colon + 1);
        if (q1 < 0) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = q1 + 1; i < obj.length(); i++) {
            char c = obj.charAt(i);
            if (c == '\\' && i + 1 < obj.length()) {
                char next = obj.charAt(++i);
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
        return sb.toString();
    }

    private static boolean boolField(String obj, String key) {
        String search = "\"" + key + "\"";
        int ki = obj.indexOf(search);
        if (ki < 0) return false;
        int colon = obj.indexOf(':', ki + search.length());
        if (colon < 0) return false;
        return obj.substring(colon + 1).stripLeading().startsWith("true");
    }

    private record CardData(String text, boolean done) {}
}