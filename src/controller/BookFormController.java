package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Book;
import service.BookService;

public class BookFormController {
    @FXML private TextField tfTitle, tfAuthor, tfPublisher, tfYear, tfGenre, tfQty, tfCover;
    @FXML private Button btnSave, btnCancel;
    @FXML private Label lblMsg;

    private final BookService bookService = new BookService();
    private Book editing = null;

    public void setEditing(Book b) {
        this.editing = b;
        if (b != null) {
            tfTitle.setText(b.getTitle());
            tfAuthor.setText(b.getAuthor());
            tfPublisher.setText(b.getPublisher());
            tfYear.setText(String.valueOf(b.getPublishYear()));
            tfGenre.setText(b.getGenre());
            tfQty.setText(String.valueOf(b.getQuantity()));
            tfCover.setText(b.getCoverImage());
        }
    }

    @FXML
    private void onSave() {
        try {
            String title = tfTitle.getText().trim();
            if (title.isEmpty()) {
                lblMsg.setText("Title required");
                return;
            }
            String author = tfAuthor.getText().trim();
            String publisher = tfPublisher.getText().trim();
            int year = Integer.parseInt(tfYear.getText().trim());
            String genre = tfGenre.getText().trim();
            int qty = Integer.parseInt(tfQty.getText().trim());
            String cover = tfCover.getText().trim();

            if (editing == null) {
                // Ajout d'un nouveau livre
                Book b = new Book(title, author, publisher, year, genre, qty, cover);
                boolean ok = bookService.addBook(b);
                lblMsg.setText(ok ? "Added" : "Add failed");
            } else {
                // Modification d'un livre existant
                editing.setTitle(title);
                editing.setAuthor(author);
                editing.setPublisher(publisher);
                editing.setPublishYear(year);
                editing.setGenre(genre);
                editing.setQuantity(qty);
                editing.setCoverImage(cover);
                boolean ok = bookService.updateBook(editing);
                lblMsg.setText(ok ? "Updated" : "Update failed");
            }

            // Fermer la fenêtre après sauvegarde
            Stage s = (Stage) btnSave.getScene().getWindow();
            s.close();
        } catch (NumberFormatException ex) {
            lblMsg.setText("Year/Qty invalid");
        }
    }

    @FXML
    private void onCancel() {
        Stage s = (Stage) btnCancel.getScene().getWindow();
        s.close();
    }
}