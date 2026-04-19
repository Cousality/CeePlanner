package cee.ceeplanner;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class   App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("views/workspace.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setMaximized(true);
        stage.setTitle("CeePlanner");
        stage.setScene(scene);
        stage.show();
    }
}