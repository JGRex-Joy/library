package library.main.library;

import javafx.fxml.FXML;
import java.io.IOException;

public class HelloController {

    @FXML
    protected void onStartButtonClick() throws IOException {
        LibraryController.changeScene();
    }
}