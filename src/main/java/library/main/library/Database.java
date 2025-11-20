package library.main.library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class Database {

    private static final String URL = "jdbc:sqlite:library.db";

    // Подключение к базе
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Создание таблицы books, если нет
    public static void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS books (
                author TEXT,
                title TEXT,
                isbn TEXT PRIMARY KEY,
                category TEXT
            );
        """;
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    // Добавление книги
    public static void addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books(author, title, isbn, category) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getAuthor());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getIsbn());
            pstmt.setString(4, book.getCategory());
            pstmt.executeUpdate();
        }
    }

    // Получение всех книг
    public static ObservableList<Book> getAllBooks() throws SQLException {
        ObservableList<Book> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM books";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Book(
                        rs.getString("author"),
                        rs.getString("title"),
                        rs.getString("isbn"),
                        rs.getString("category")
                ));
            }
        }
        return list;
    }

    // Удаление книги по ISBN
    public static void deleteBook(String isbn) throws SQLException {
        String sql = "DELETE FROM books WHERE isbn = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            pstmt.executeUpdate();
        }
    }

    // Редактирование книги
    public static void updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET author=?, title=?, category=? WHERE isbn=?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getAuthor());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getCategory());
            pstmt.setString(4, book.getIsbn());
            pstmt.executeUpdate();
        }
    }

    // Поиск книг по тексту
    public static ObservableList<Book> searchBooks(String text) throws SQLException {
        ObservableList<Book> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM books WHERE " +
                "LOWER(author) LIKE ? OR LOWER(title) LIKE ? OR LOWER(isbn) LIKE ? OR LOWER(category) LIKE ?";
        String queryText = "%" + text.toLowerCase() + "%";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, queryText);
            pstmt.setString(2, queryText);
            pstmt.setString(3, queryText);
            pstmt.setString(4, queryText);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Book(
                        rs.getString("author"),
                        rs.getString("title"),
                        rs.getString("isbn"),
                        rs.getString("category")
                ));
            }
        }
        return list;
    }
}
