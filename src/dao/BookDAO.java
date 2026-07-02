package dao;

import model.Book;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    private Connection connection;

    public BookDAO() {
        this.connection = DBConnection.getConnection();
    }

    // Insérer un livre
    public boolean insert(Book book) {
        String query = "INSERT INTO books (title, author, publisher, publish_year, genre, quantity, cover_image) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getPublisher());  // ✅ AJOUTÉ
            pstmt.setInt(4, book.getPublishYear());
            pstmt.setString(5, book.getGenre());
            pstmt.setInt(6, book.getQuantity());
            pstmt.setString(7, book.getCoverImage());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Obtenir tous les livres
    public List<Book> getAll() {
        return getAllBooks();
    }

    // Obtenir tous les livres
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books ORDER BY title";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Book book = new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),  // ✅ AJOUTÉ
                    rs.getInt("publish_year"),
                    rs.getString("genre"),
                    rs.getInt("quantity"),
                    rs.getString("cover_image")
                );
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    // Compter le nombre total de livres
    public int countAll() {
        String query = "SELECT COUNT(*) as total FROM books";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Rechercher des livres par titre
    public List<Book> searchByTitle(String title) {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books WHERE title LIKE ? ORDER BY title";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, "%" + title + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Book book = new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),  // ✅ AJOUTÉ
                    rs.getInt("publish_year"),
                    rs.getString("genre"),
                    rs.getInt("quantity"),
                    rs.getString("cover_image")
                );
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    // Obtenir les livres disponibles (quantité > 0)
    public List<Book> getAvailableBooks() {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books WHERE quantity > 0 ORDER BY title";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Book book = new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),  // ✅ AJOUTÉ
                    rs.getInt("publish_year"),
                    rs.getString("genre"),
                    rs.getInt("quantity"),
                    rs.getString("cover_image")
                );
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    // Obtenir un livre par ID
    public Book getById(int id) {
        String query = "SELECT * FROM books WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),  // ✅ AJOUTÉ
                    rs.getInt("publish_year"),
                    rs.getString("genre"),
                    rs.getInt("quantity"),
                    rs.getString("cover_image")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Obtenir le livre le plus emprunté
    public Book getMostBorrowedBook() {
        String query = "SELECT b.* FROM books b " +
                       "JOIN loans l ON b.id = l.book_id " +
                       "GROUP BY b.id " +
                       "ORDER BY COUNT(l.id) DESC LIMIT 1";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),  // ✅ AJOUTÉ
                    rs.getInt("publish_year"),
                    rs.getString("genre"),
                    rs.getInt("quantity"),
                    rs.getString("cover_image")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Mettre à jour un livre
    public boolean update(Book book) {
        String query = "UPDATE books SET title = ?, author = ?, publisher = ?, publish_year = ?, genre = ?, quantity = ?, cover_image = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getPublisher());  // ✅ AJOUTÉ
            pstmt.setInt(4, book.getPublishYear());
            pstmt.setString(5, book.getGenre());
            pstmt.setInt(6, book.getQuantity());
            pstmt.setString(7, book.getCoverImage());
            pstmt.setInt(8, book.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Supprimer un livre
    public boolean delete(int id) {
        String query = "DELETE FROM books WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Augmenter la quantité
    public boolean increaseQuantity(int bookId) {
        String query = "UPDATE books SET quantity = quantity + 1 WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, bookId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Diminuer la quantité
    public boolean decreaseQuantity(int bookId) {
        String query = "UPDATE books SET quantity = quantity - 1 WHERE id = ? AND quantity > 0";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, bookId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}