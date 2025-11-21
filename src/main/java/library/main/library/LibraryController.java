package library.main.library;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LibraryController implements Initializable {

    private int userId;

    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, String> colAuthor;
    @FXML private TableColumn<Book, String> colTitle;
    @FXML private TableColumn<Book, String> colIsbn;
    @FXML private TableColumn<Book, String> colCategory;

    @FXML private TextField author;
    @FXML private TextField book;
    @FXML private TextField isbn;
    @FXML private TextField category;
    @FXML private TextField search;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Колонки
        colAuthor.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getAuthor()));
        colTitle.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getTitle()));
        colIsbn.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getIsbn()));
        colCategory.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getCategory()));

        booksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // При выборе строки заполнить поля
        booksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                author.setText(newSel.getAuthor());
                book.setText(newSel.getTitle());
                isbn.setText(newSel.getIsbn());
                isbn.setEditable(false);
                category.setText(newSel.getCategory());
            } else {
                clearForm();
            }
        });
    }

    public void setUser(int userId) {
        this.userId = userId;
        try {
            loadBooks();
        } catch (SQLException e) {
            showError("DB Error", e.getMessage());
        }
    }

    @FXML
    protected void loadBooks() throws SQLException {
        ObservableList<Book> list = Database.getAllBooksForUser(userId);
        booksTable.setItems(list);
        if (list.isEmpty()) clearForm();
    }

    @FXML
    protected void addItem() {
        String a = author.getText().trim();
        String t = book.getText().trim();
        String i = isbn.getText().trim();
        String c = category.getText().trim();

        if (a.isEmpty() || t.isEmpty() || i.isEmpty()) {
            showWarning("Validation", "Fields Author, Title and ISBN are required.");
            return;
        }

        try {
            Database.addBookForUser(new Book(a, t, i, c), userId);
            showInfo("Success", "Book added.");
            clearForm();
            loadBooks();
        } catch (SQLException e) {
            showError("DB Error", e.getMessage());
        }
    }

    @FXML
    protected void deleteBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Select book", "Choose book to delete.");
            return;
        }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete book: " + selected.getTitle() + "?",
                ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    Database.deleteBookForUser(selected.getIsbn(), userId);
                    showInfo("Deleted", "Book removed.");
                    clearForm();
                    loadBooks();
                } catch (SQLException e) {
                    showError("DB Error", e.getMessage());
                }
            }
        });
    }

    @FXML
    protected void editBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Select book", "Choose a book to edit.");
            return;
        }

        String a = author.getText().trim();
        String t = book.getText().trim();
        String i = isbn.getText().trim();
        String c = category.getText().trim();

        if (a.isEmpty() || t.isEmpty() || i.isEmpty()) {
            showWarning("Validation", "Author, Title and ISBN are required.");
            return;
        }

        if (!i.equals(selected.getIsbn())) {
            showWarning("ISBN change", "Changing ISBN is not allowed.");
            return;
        }

        try {
            Database.updateBookForUser(new Book(a, t, i, c), userId);
            showInfo("Updated", "Book updated.");
            clearForm();
            loadBooks();
        } catch (SQLException e) {
            showError("DB Error", e.getMessage());
        }
    }

    @FXML
    protected void searchBook() {
        String text = search.getText().trim();

        try {
            if (text.length() < 2) {
                loadBooks();
                return;
            }
            booksTable.setItems(Database.searchBooksForUser(text, userId));

        } catch (SQLException e) {
            showError("DB Error", e.getMessage());
        }
    }

    private void clearForm() {
        author.clear();
        book.clear();
        isbn.clear();
        isbn.setEditable(true);
        category.clear();
        booksTable.getSelectionModel().clearSelection();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(title);
        a.showAndWait();
    }

    private void showWarning(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setTitle(title);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.showAndWait();
    }

    public static void changeScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("library.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = HelloApplication.getPrimaryStage();
        stage.setScene(scene);
        stage.show();
    }
}
