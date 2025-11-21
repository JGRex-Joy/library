package library.main.library.models;

public class Book {
    private String author;
    private String title;
    private String isbn;
    private String category;

    public Book(String author, String title, String isbn, String category) {
        this.author = author;
        this.title = title;
        this.isbn = isbn;
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getCategory() {
        return category;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
