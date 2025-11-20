package library.main.library;

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
}
