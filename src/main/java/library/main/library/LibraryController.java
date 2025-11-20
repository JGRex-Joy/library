package library.main.library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

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

    private ObservableList<Book> bookData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        colAuthor.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getAuthor()));
        colTitle.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getTitle()));
        colIsbn.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getIsbn()));
        colCategory.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getCategory()));

        booksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        try {
            loadBooks();
        } catch (IOException e) {
            e.printStackTrace();
        }

        booksTable.setItems(bookData);
    }

    /** Load books from /data folder */
    public static ArrayList<Book> readFolder(File folder) throws IOException {
        ArrayList<Book> list = new ArrayList<>();

        for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            String row = Files.readAllLines(Paths.get(fileEntry.getPath())).get(0);

            String[] parts = row.split("\\|");
            if (parts.length == 4) {
                list.add(new Book(
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim()
                ));
            }
        }
        return list;
    }

    public void loadBooks() throws IOException {
        File folder = new File("src/main/data");

        bookData.clear();
        bookData.addAll(readFolder(folder));
        booksTable.refresh();
    }

    @FXML
    protected void searchBook() throws IOException {

        String search_text = search.getText().strip().toLowerCase();

        loadBooks();

        if (search_text.length() >= 3) {
            List<Book> results = bookData.stream()
                    .filter(b ->
                            b.getAuthor().toLowerCase().contains(search_text) ||
                                    b.getTitle().toLowerCase().contains(search_text) ||
                                    b.getIsbn().toLowerCase().contains(search_text) ||
                                    b.getCategory().toLowerCase().contains(search_text)
                    )
                    .collect(Collectors.toList());

            bookData.setAll(results);
        }
    }

    @FXML
    protected void deleteBook() throws IOException {

        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Path p = Paths.get("src/main/data/" + selected.getIsbn() + ".txt");
        Files.deleteIfExists(p);

        loadBooks();
        search.setText("");
    }

    @FXML
    protected void addItem() throws IOException {

        String author_text = author.getText();
        String book_text = book.getText();
        String isbn_text = isbn.getText();
        String category_text = category.getText();

        String data = author_text + " | " + book_text + " | " + isbn_text + " | " + category_text;

        Path p = Paths.get("src/main/data/" + isbn_text + ".txt");

        if (!Files.exists(p)) {
            Files.write(p, data.getBytes());
        }

        author.clear();
        book.clear();
        isbn.clear();
        category.clear();

        loadBooks();
    }

    @FXML
    protected void editBook() throws IOException {

        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String oldIsbn = selected.getIsbn();

        EditBook eb = new EditBook();
        String result = eb.getResult();

        if (result != null) {

            Path oldFile = Paths.get("src/main/data/" + oldIsbn + ".txt");
            Files.deleteIfExists(oldFile);

            String[] parts = result.split(";");
            Path newFile = Paths.get("src/main/data/" + parts[2] + ".txt");

            Files.write(newFile, result.getBytes());

            loadBooks();
            search.setText("");
        }
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
