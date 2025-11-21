package library.main.library.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import library.main.library.database.Database;
import library.main.library.MainApp;

import java.sql.SQLException;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    protected void onRegister() {
        String u = usernameField.getText().trim();
        String p = passwordField.getText().trim();
        String c = confirmPasswordField.getText().trim();

        if (u.isEmpty() || p.isEmpty()) {
            showError("Validation", "Username and password required");
            return;
        }
        if (!p.equals(c)) {
            showError("Validation", "Passwords do not match");
            return;
        }

        try {
            boolean ok = Database.registerUser(u, p);
            if (ok) {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Success");
                a.setHeaderText(null);
                a.setContentText("Account created. Please login");
                a.showAndWait();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/library/main/library/register.fxml"
                ));
                Scene scene = new Scene(loader.load());
                MainApp.getPrimaryStage().setScene(scene);
            } else {
                showError("Register failed", "Username already exists");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database error", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", e.getMessage());
        }
    }

    @FXML
    protected void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/library/main/library/login.fxml"));
            Scene scene = new Scene(loader.load());
            MainApp.getPrimaryStage().setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation error", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
