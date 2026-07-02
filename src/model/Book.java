package model;

public class Book {
    private int id;
    private String title;
    private String author;
    private String publisher;      // ✅ AJOUTÉ
    private int publishYear;
    private String genre;
    private int quantity;
    private String coverImage;

    // Constructeur vide
    public Book() {
    }

    // Constructeur sans ID (pour l'ajout)
    public Book(String title, String author, String publisher, int publishYear, String genre, int quantity, String coverImage) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;  // ✅ AJOUTÉ
        this.publishYear = publishYear;
        this.genre = genre;
        this.quantity = quantity;
        this.coverImage = coverImage;
    }

    // Constructeur complet avec ID
    public Book(int id, String title, String author, String publisher, int publishYear, String genre, int quantity, String coverImage) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;  // ✅ AJOUTÉ
        this.publishYear = publishYear;
        this.genre = genre;
        this.quantity = quantity;
        this.coverImage = coverImage;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    // ✅ MÉTHODES AJOUTÉES
    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(int publishYear) {
        this.publishYear = publishYear;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", publisher='" + publisher + '\'' +  // ✅ AJOUTÉ
                ", publishYear=" + publishYear +
                ", genre='" + genre + '\'' +
                ", quantity=" + quantity +
                ", coverImage='" + coverImage + '\'' +
                '}';
    }
}