package library.main.library.controllers;

import javafx.fxml.FXML;

import java.io.IOException;

public class HelloController {

    @FXML
    protected void onStartButtonClick() throws IOException {
        LibraryController.changeScene();
    }
}