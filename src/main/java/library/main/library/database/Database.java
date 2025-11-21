package library.main.library.database;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import library.main.library.models.Book;

import java.sql.*;

public class Database {

    private static final String URL = "jdbc:sqlite:library.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void createTables() throws SQLException {
        String usersSql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL
            );
            """;

        String booksSql = """
            CREATE TABLE IF NOT EXISTS books (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                author TEXT,
                title TEXT,
                isbn TEXT,
                category TEXT,
                user_id INTEGER,
                FOREIGN KEY(user_id) REFERENCES users(id)
            );
            """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(usersSql);
            stmt.execute(booksSql);
        }
    }

    public static boolean registerUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO users(username, password) VALUES (?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean validateUser(String username, String password) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static int getUserId(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
                else return -1;
            }
        }
    }


    public static void addBookForUser(Book book, int userId) throws SQLException {
        String sql = "INSERT INTO books(author, title, isbn, category, user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getAuthor());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getIsbn());
            pstmt.setString(4, book.getCategory());
            pstmt.setInt(5, userId);
            pstmt.executeUpdate();
        }
    }

    public static ObservableList<Book> getAllBooksForUser(int userId) throws SQLException {
        ObservableList<Book> list = FXCollections.observableArrayList();
        String sql = "SELECT author, title, isbn, category FROM books WHERE user_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Book(
                            rs.getString("author"),
                            rs.getString("title"),
                            rs.getString("isbn"),
                            rs.getString("category")
                    ));
                }
            }
        }
        return list;
    }

    public static void deleteBookForUser(String isbn, int userId) throws SQLException {
        String sql = "DELETE FROM books WHERE isbn = ? AND user_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    public static void updateBookForUser(Book book, int userId) throws SQLException {
        String sql = "UPDATE books SET author = ?, title = ?, category = ? WHERE isbn = ? AND user_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getAuthor());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getCategory());
            pstmt.setString(4, book.getIsbn());
            pstmt.setInt(5, userId);
            pstmt.executeUpdate();
        }
    }

    public static ObservableList<Book> searchBooksForUser(String text, int userId) throws SQLException {
        ObservableList<Book> list = FXCollections.observableArrayList();
        String sql = "SELECT author, title, isbn, category FROM books WHERE user_id = ? AND (" +
                "LOWER(author) LIKE ? OR LOWER(title) LIKE ? OR LOWER(isbn) LIKE ? OR LOWER(category) LIKE ?)";
        String q = "%" + text.toLowerCase() + "%";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, q);
            pstmt.setString(3, q);
            pstmt.setString(4, q);
            pstmt.setString(5, q);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Book(
                            rs.getString("author"),
                            rs.getString("title"),
                            rs.getString("isbn"),
                            rs.getString("category")
                    ));
                }
            }
        }
        return list;
    }
}
