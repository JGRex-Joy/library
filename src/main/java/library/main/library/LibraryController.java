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
        // Инициализация колонок
        colAuthor.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getAuthor()));
        colTitle.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getTitle()));
        colIsbn.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getIsbn()));
        colCategory.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getCategory()));

        // Авто-ширина колонок, чтобы убрать горизонтальный скролл
        booksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // При выборе строки — заполняем поля формы
        booksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                author.setText(newSel.getAuthor());
                book.setText(newSel.getTitle());
                isbn.setText(newSel.getIsbn());
                isbn.setEditable(false); // предотвращаем изменение PK
                category.setText(newSel.getCategory());
            } else {
                clearForm();
            }
        });

        try {
            Database.createTable();  // создаем таблицу, если нет
            loadBooks();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database error", e.getMessage());
        }
    }

    @FXML
    protected void loadBooks() throws SQLException {
        ObservableList<Book> list = Database.getAllBooks();
        booksTable.setItems(list);
        // если список пуст, очистим форму и разрешим редактирование ISBN
        if (list.isEmpty()) {
            clearForm();
        }
    }

    @FXML
    protected void addItem() {
        String a = author.getText().trim();
        String t = book.getText().trim();
        String i = isbn.getText().trim();
        String c = category.getText().trim();

        if (a.isEmpty() || t.isEmpty() || i.isEmpty()) {
            showWarning("Validation", "Author, Title and ISBN are required.");
            return;
        }

        try {
            Book b = new Book(a, t, i, c);
            Database.addBook(b);
            showInfo("Added", "Book added successfully.");
            clearForm();
            loadBooks();
        } catch (SQLException e) {
            // если ISBN уже есть, покажем ошибку
            showError("Database error", e.getMessage());
        }
    }

    @FXML
    protected void deleteBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selection", "Please select a book to delete.");
            return;
        }

        // подтверждение удаления
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete book with ISBN: " + selected.getIsbn() + "?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm delete");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    Database.deleteBook(selected.getIsbn());
                    showInfo("Deleted", "Book deleted.");
                    clearForm();
                    loadBooks();
                } catch (SQLException e) {
                    showError("Database error", e.getMessage());
                }
            }
        });
    }

    @FXML
    protected void editBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selection", "Please select a book to edit.");
            return;
        }

        // Берём значения из полей (isbn ранее заблокирован для редактирования)
        String newAuthor = author.getText().trim();
        String newTitle = book.getText().trim();
        String newIsbn = isbn.getText().trim();
        String newCategory = category.getText().trim();

        if (newAuthor.isEmpty() || newTitle.isEmpty() || newIsbn.isEmpty()) {
            showWarning("Validation", "Author, Title and ISBN are required.");
            return;
        }

        // Если пользователь каким-то образом изменил ISBN (обычно заблокировано) — предупредим
        if (!newIsbn.equals(selected.getIsbn())) {
            // Редактирование ISBN как PK — непросто: лучше удалить+вставить или запретить
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Change ISBN");
            a.setHeaderText(null);
            a.setContentText("You changed ISBN. ISBN is primary key. Do you want to create a new record with new ISBN and delete the old one?");
            a.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            a.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    try {
                        // создаём новую запись и удаляем старую
                        Database.addBook(new Book(newAuthor, newTitle, newIsbn, newCategory));
                        Database.deleteBook(selected.getIsbn());
                        showInfo("Updated", "Book updated (ISBN changed).");
                        clearForm();
                        loadBooks();
                    } catch (SQLException e) {
                        showError("Database error", e.getMessage());
                    }
                }
            });
            return;
        }

        // Обычное обновление (ISBN не меняется)
        try {
            Book updated = new Book(newAuthor, newTitle, newIsbn, newCategory);
            Database.updateBook(updated);
            showInfo("Updated", "Book updated successfully.");
            clearForm();
            loadBooks();
        } catch (SQLException e) {
            showError("Database error", e.getMessage());
        }
    }

    @FXML
    protected void searchBook() {
        String text = search.getText().trim();
        try {
            if (text.length() >= 3) {
                ObservableList<Book> results = Database.searchBooks(text);
                booksTable.setItems(results);
            } else {
                loadBooks();
            }
        } catch (SQLException e) {
            showError("Database error", e.getMessage());
        }
    }

    // Удобный метод очистки формы и разблокировки isbn
    private void clearForm() {
        author.clear();
        book.clear();
        isbn.clear();
        isbn.setEditable(true);
        category.clear();
        booksTable.getSelectionModel().clearSelection();
    }

    // Вспомогательные диалоги
    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    public static void changeScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("library.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = HelloApplication.getPrimaryStage();
        stage.hide();
        stage.setTitle("Library Management System");
        stage.setScene(scene);
        stage.show();
    }
}
