package service;

import dao.BookDAO;
import model.Book;

import java.util.List;

public class BookService {

    private final BookDAO dao = new BookDAO();

    public boolean addBook(Book b) { return dao.insert(b); }
    public boolean updateBook(Book b) { return dao.update(b); }
    public boolean deleteBook(int id) { return dao.delete(id); }
    public List<Book> getAllBooks() { return dao.getAll(); }
    public List<Book> searchBooksByTitle(String title) { return dao.searchByTitle(title); }
    public Book getById(int id) { return dao.getById(id); }
    public List<Book> getAvailableBooks() { return dao.getAvailableBooks(); }
    public boolean decreaseQuantity(int id) { return dao.decreaseQuantity(id); }
    public boolean increaseQuantity(int id) { return dao.increaseQuantity(id); }
    public int getTotalBooks() { return dao.countAll(); }
    public Book getMostBorrowedBook() { return dao.getMostBorrowedBook(); }
}
