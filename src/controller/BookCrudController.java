package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Book;
import service.BookService;
import utils.ViewHelper;

import java.util.List;

public class BookCrudController {
    
    @FXML private TableView<Book> table;
    @FXML private TextField tfSearch;
    @FXML private Button btnAdd, btnEdit, btnDelete;
    @FXML private Label lblMsg;
    
    private final BookService bookService = new BookService();
    private final ObservableList<Book> items = FXCollections.observableArrayList();
    
    @FXML 
    private void initialize() {
        System.out.println("🔵 BookCrudController - initialize() appelée");
        loadAll();
    }
    
    private void loadAll() {
        System.out.println("📚 Chargement des livres...");
        items.clear();
        
        try {
            List<Book> all = bookService.getAllBooks();
            System.out.println("   Livres trouvés: " + (all != null ? all.size() : 0));
            
            if (all != null && !all.isEmpty()) {
                items.addAll(all);
                table.setItems(items);
                System.out.println("✅ " + items.size() + " livres chargés dans le tableau");
                
                // DEBUG: Afficher les 3 premiers livres
                for (int i = 0; i < Math.min(3, all.size()); i++) {
                    Book b = all.get(i);
                    System.out.println("   - Livre " + (i+1) + ": " + b.getTitle() + " par " + b.getAuthor());
                }
            } else {
                System.out.println("⚠️  Aucun livre trouvé dans la base de données");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des livres:");
            e.printStackTrace();
        }
    }
    
    @FXML 
    private void onSearch() {
        String q = tfSearch.getText().trim();
        System.out.println("🔍 Recherche: '" + q + "'");
        
        if (q.isEmpty()) {
            loadAll();
        } else {
            List<Book> results = bookService.searchBooksByTitle(q);
            items.clear();
            if (results != null) {
                items.addAll(results);
            }
            table.setItems(items);
            System.out.println("   Résultats: " + items.size() + " livre(s)");
        }
    }
    
    @FXML 
    private void onAdd() {
        System.out.println("➕ Ouverture formulaire ajout livre");
        openBookForm(null);
        loadAll();
    }
    
    @FXML 
    private void onEdit() {
        Book sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { 
            lblMsg.setText("Sélectionnez un livre");
            System.out.println("⚠️  Aucun livre sélectionné pour modification");
            return; 
        }
        
        System.out.println("✏️  Modification du livre: " + sel.getTitle());
        openBookForm(sel);
        loadAll();
    }
    
    /**
     * ✅ NOUVELLE MÉTHODE - Ouvre le formulaire et passe le livre à modifier
     */
    private void openBookForm(Book bookToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BookForm.fxml"));
            Parent root = loader.load();
            
            // Récupérer le contrôleur et passer le livre
            BookFormController controller = loader.getController();
            if (bookToEdit != null) {
                controller.setEditing(bookToEdit);
                System.out.println("✅ Livre passé au formulaire: " + bookToEdit.getTitle());
            }
            
            // Créer et afficher la fenêtre modale
            Stage stage = new Stage();
            stage.setTitle(bookToEdit == null ? "Ajouter un livre" : "Modifier un livre");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnEdit.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture formulaire:");
            e.printStackTrace();
            lblMsg.setText("Erreur ouverture formulaire");
        }
    }
    
    @FXML 
    private void onDelete() {
        Book sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { 
            lblMsg.setText("Sélectionnez un livre");
            System.out.println("⚠️  Aucun livre sélectionné pour suppression");
            return; 
        }
        
        System.out.println("🗑️  Suppression du livre: " + sel.getTitle());
        
        // Confirmation de suppression
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer ce livre ?");
        confirm.setContentText("Titre: " + sel.getTitle() + "\nAuteur: " + sel.getAuthor());
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean ok = bookService.deleteBook(sel.getId());
            lblMsg.setText(ok ? "Livre supprimé" : "Échec de la suppression");
            
            if (ok) {
                System.out.println("✅ Livre supprimé avec succès");
            } else {
                System.out.println("❌ Échec de la suppression");
            }
            
            loadAll();
        }
    }
}