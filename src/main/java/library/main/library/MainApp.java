package library.main.library;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import library.main.library.database.Database;

public class MainApp extends Application {

    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        Database.createTables();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/library/main/library/login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Library - Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
