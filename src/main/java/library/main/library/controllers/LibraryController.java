package library.main.library.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import library.main.library.models.Book;
import library.main.library.database.Database;
import library.main.library.HelloApplication;

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
        setupColumns();
        booksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        booksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> fillForm(newSel));
    }

    public void setUser(int userId) {
        this.userId = userId;
        try { loadBooks(); }
        catch (SQLException e) { showError("DB Error", e.getMessage()); }
    }

    @FXML
    protected void loadBooks() throws SQLException {
        ObservableList<Book> list = Database.getAllBooksForUser(userId);
        booksTable.setItems(list);
        if (list.isEmpty()) clearForm();
    }

    @FXML
    protected void addItem() {
        Book bookObj = getFormData();
        if (bookObj == null) return;
        try {
            Database.addBookForUser(bookObj, userId);
            showInfo("Success", "Book added");
            reload();
        } catch (SQLException e) { showError("DB Error", e.getMessage()); }
    }

    @FXML
    protected void deleteBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("Select book", "Choose book to delete"); return; }

        if (confirmAction("Delete book", "Delete book: " + selected.getTitle() + "?")) {
            try {
                Database.deleteBookForUser(selected.getIsbn(), userId);
                showInfo("Deleted", "Book removed");
                reload();
            } catch (SQLException e) { showError("DB Error", e.getMessage()); }
        }
    }

    @FXML
    protected void editBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        Book bookObj = getFormData();
        if (selected == null) { showWarning("Select book", "Choose a book to edit."); return; }
        if (bookObj == null) return;
        if (!bookObj.getIsbn().equals(selected.getIsbn())) {
            showWarning("ISBN change", "Changing ISBN is not allowed.");
            return;
        }

        try {
            Database.updateBookForUser(bookObj, userId);
            showInfo("Updated", "Book updated.");
            reload();
        } catch (SQLException e) { showError("DB Error", e.getMessage()); }
    }

    @FXML
    protected void searchBook() {
        String text = search.getText().trim();
        try {
            if (text.length() < 2) { loadBooks(); return; }
            booksTable.setItems(Database.searchBooksForUser(text, userId));
        } catch (SQLException e) { showError("DB Error", e.getMessage()); }
    }

    private void setupColumns() {
        colAuthor.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getAuthor()));
        colTitle.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getTitle()));
        colIsbn.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getIsbn()));
        colCategory.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getCategory()));
    }

    private void fillForm(Book b) {
        if (b == null) { clearForm(); return; }
        author.setText(b.getAuthor());
        book.setText(b.getTitle());
        isbn.setText(b.getIsbn());
        isbn.setEditable(false);
        category.setText(b.getCategory());
    }

    private Book getFormData() {
        String a = author.getText().trim();
        String t = book.getText().trim();
        String i = isbn.getText().trim();
        String c = category.getText().trim();
        if (a.isEmpty() || t.isEmpty() || i.isEmpty()) {
            showWarning("Validation", "Fields Author, Title and ISBN are required");
            return null;
        }
        return new Book(a, t, i, c);
    }

    private void clearForm() {
        author.clear();
        book.clear();
        isbn.clear();
        isbn.setEditable(true);
        category.clear();
        booksTable.getSelectionModel().clearSelection();
    }

    private boolean confirmAction(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setTitle(title);
        return a.showAndWait().filter(btn -> btn == ButtonType.YES).isPresent();
    }

    private void showError(String title, String msg) { showAlert(Alert.AlertType.ERROR, title, msg); }
    private void showWarning(String title, String msg) { showAlert(Alert.AlertType.WARNING, title, msg); }
    private void showInfo(String title, String msg) { showAlert(Alert.AlertType.INFORMATION, title, msg); }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.setTitle(title);
        a.showAndWait();
    }

    private void reload() throws SQLException {
        clearForm();
        loadBooks();
    }

    public static void changeScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("library.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = HelloApplication.getPrimaryStage();
        stage.setScene(scene);
        stage.show();
    }
}
