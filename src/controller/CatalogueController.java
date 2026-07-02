package controller;

import dao.BookDAO;
import dao.LoanDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Book;
import model.Loan;
import model.UserAccount;
import utils.ViewHelper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ✅ Controller complet pour Catalogue.fxml
 * - Affiche UNIQUEMENT les livres disponibles (quantity > 0)
 * - Permet aux membres d'emprunter des livres
 * - Recherche par titre
 * - Vérification des droits d'accès
 */
public class CatalogueController {
    
    @FXML private TextField tfSearch;
    @FXML private TableView<Book> table;
    @FXML private Label lblMsg;
    @FXML private Button btnBorrow;
    
    private final BookDAO bookDAO = new BookDAO();
    private final LoanDAO loanDAO = new LoanDAO();
    private ObservableList<Book> allBooks;
    
    @FXML
    public void initialize() {
        System.out.println("\n=== INITIALISATION CATALOGUE ===");
        
        // Vérifier l'authentification
        if (!SessionController.isAuthenticated()) {
            showErrorAndReturn("Veuillez vous connecter pour voir le catalogue.");
            return;
        }
        
        UserAccount currentUser = SessionController.getCurrentUser();
        System.out.println("✅ Utilisateur: " + currentUser.getFullName() + " (" + currentUser.getRoleName() + ")");
        
        // Configurer les colonnes du tableau
        setupTableColumns();
        
        // Charger les livres disponibles
        loadAvailableBooks();
        
        // Vérifier les droits d'emprunt
        if (!SessionController.hasRole("MEMBER") && !SessionController.hasRole("LIBRARIAN")) {
            btnBorrow.setDisable(true);
            lblMsg.setText("⚠️ Vous n'êtes pas autorisé à emprunter des livres");
        }
        
        System.out.println("=== FIN INITIALISATION ===\n");
    }
    
    /**
     * Configure les colonnes du TableView
     */
    private void setupTableColumns() {
        // La configuration est déjà dans le FXML, mais on peut ajouter des styles
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Style pour la colonne quantité (mettre en rouge si 0)
        TableColumn<Book, Integer> qtyColumn = (TableColumn<Book, Integer>) table.getColumns().get(5);
        qtyColumn.setCellFactory(column -> new TableCell<Book, Integer>() {
            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(quantity));
                    if (quantity == 0) {
                        setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;");
                    } else if (quantity < 3) {
                        setStyle("-fx-text-fill: #ed8936; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #48bb78; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }
    
    /**
     * ✅ Charge UNIQUEMENT les livres disponibles (quantity > 0)
     */
    private void loadAvailableBooks() {
        try {
            System.out.println("\n📚 Chargement des livres disponibles...");
            
            // Récupérer tous les livres disponibles
            List<Book> availableBooks = bookDAO.getAvailableBooks();
            
            if (availableBooks.isEmpty()) {
                lblMsg.setText("⚠️ Aucun livre disponible pour le moment");
                lblMsg.setStyle("-fx-text-fill: #ed8936; -fx-font-size: 14px; -fx-font-weight: bold;");
                System.out.println("⚠️ Aucun livre disponible dans la base");
            } else {
                lblMsg.setText("✅ " + availableBooks.size() + " livre(s) disponible(s)");
                lblMsg.setStyle("-fx-text-fill: #48bb78; -fx-font-size: 14px; -fx-font-weight: bold;");
                System.out.println("✅ " + availableBooks.size() + " livres chargés");
                
                // Afficher les détails
                for (Book book : availableBooks) {
                    System.out.println("   - " + book.getTitle() + " (Qty: " + book.getQuantity() + ")");
                }
            }
            
            allBooks = FXCollections.observableArrayList(availableBooks);
            table.setItems(allBooks);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des livres");
            e.printStackTrace();
            lblMsg.setText("❌ Erreur lors du chargement des livres");
            lblMsg.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 14px; -fx-font-weight: bold;");
        }
    }
    
    /**
     * Recherche des livres par titre
     */
    @FXML
    private void onSearch() {
        String query = tfSearch.getText().trim();
        
        if (query.isEmpty()) {
            // Recharger tous les livres disponibles
            loadAvailableBooks();
            return;
        }
        
        try {
            System.out.println("\n🔍 Recherche: " + query);
            
            // Rechercher dans tous les livres
            List<Book> allSearchResults = bookDAO.searchByTitle(query);
            
            // Filtrer uniquement les livres disponibles
            List<Book> availableResults = allSearchResults.stream()
                .filter(book -> book.getQuantity() > 0)
                .toList();
            
            if (availableResults.isEmpty()) {
                lblMsg.setText("⚠️ Aucun livre disponible correspondant à: " + query);
                lblMsg.setStyle("-fx-text-fill: #ed8936; -fx-font-size: 14px;");
                table.setItems(FXCollections.observableArrayList());
            } else {
                lblMsg.setText("✅ " + availableResults.size() + " livre(s) trouvé(s) pour: " + query);
                lblMsg.setStyle("-fx-text-fill: #48bb78; -fx-font-size: 14px;");
                table.setItems(FXCollections.observableArrayList(availableResults));
            }
            
            System.out.println("✅ " + availableResults.size() + " résultats disponibles");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche");
            e.printStackTrace();
            lblMsg.setText("❌ Erreur lors de la recherche");
            lblMsg.setStyle("-fx-text-fill: #e53e3e;");
        }
    }
    
    /**
     * ✅ Emprunter un livre sélectionné
     */
    @FXML
    private void onBorrow() {
        // Vérifier les droits
        if (!SessionController.hasRole("MEMBER")) {
            showAlert("Accès refusé", "Seuls les membres peuvent emprunter des livres.", Alert.AlertType.WARNING);
            return;
        }
        
        // Vérifier qu'un livre est sélectionné
        Book selectedBook = table.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner un livre à emprunter.", Alert.AlertType.WARNING);
            return;
        }
        
        // Vérifier la disponibilité
        if (selectedBook.getQuantity() <= 0) {
            showAlert("Livre indisponible", "Ce livre n'est plus disponible pour le moment.", Alert.AlertType.ERROR);
            return;
        }
        
        // Vérifier le nombre d'emprunts actifs de l'utilisateur
        UserAccount currentUser = SessionController.getCurrentUser();
        List<Loan> userLoans = loanDAO.findByUserId(currentUser.getId());
        long activeLoans = userLoans.stream()
            .filter(loan -> !loan.isReturned() && loan.isValidated())
            .count();
        
        if (activeLoans >= 5) {
            showAlert("Limite atteinte", 
                "Vous avez atteint la limite de 5 emprunts simultanés.\n" +
                "Veuillez retourner un livre avant d'en emprunter un nouveau.", 
                Alert.AlertType.WARNING);
            return;
        }
        
        // Confirmer l'emprunt
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer l'emprunt");
        confirm.setHeaderText("Emprunter ce livre ?");
        confirm.setContentText(
            "Titre: " + selectedBook.getTitle() + "\n" +
            "Auteur: " + selectedBook.getAuthor() + "\n\n" +
            "Durée de l'emprunt: 14 jours\n" +
            "Pénalité en cas de retard: 5 MAD/jour\n\n" +
            "Voulez-vous continuer ?"
        );
        
        Optional<ButtonType> result = confirm.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            createLoanRequest(selectedBook);
        }
    }
    
    /**
     * Crée une demande d'emprunt
     */
    private void createLoanRequest(Book book) {
        try {
            UserAccount currentUser = SessionController.getCurrentUser();
            
            // Créer la demande d'emprunt (non validée)
            boolean success = loanDAO.createLoanRequest(currentUser.getId(), book.getId());
            
            if (success) {
                showAlert("Demande envoyée", 
                    "Votre demande d'emprunt pour « " + book.getTitle() + " » a été envoyée.\n\n" +
                    "Elle sera traitée par un bibliothécaire dans les plus brefs délais.\n" +
                    "Vous serez notifié une fois la demande validée.", 
                    Alert.AlertType.INFORMATION);
                
                System.out.println("✅ Demande d'emprunt créée: User #" + currentUser.getId() + " - Book #" + book.getId());
                
                // Rafraîchir le catalogue
                loadAvailableBooks();
                
            } else {
                showAlert("Erreur", 
                    "Impossible de créer la demande d'emprunt.\n" +
                    "Veuillez réessayer plus tard.", 
                    Alert.AlertType.ERROR);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de la demande");
            e.printStackTrace();
            showAlert("Erreur", "Une erreur s'est produite: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Déconnexion
     */
    @FXML
    private void onLogout() {
        System.out.println("\n🔴 Déconnexion depuis le catalogue");
        SessionController.clearSession();
        Stage stage = (Stage) tfSearch.getScene().getWindow();
        ViewHelper.show(stage, "/view/LoginView.fxml", "Connexion - Library EMSI");
    }
    
    /**
     * Affiche une erreur et retourne à la page de connexion
     */
    private void showErrorAndReturn(String msg) {
        showAlert("Erreur", msg, Alert.AlertType.ERROR);
        Stage stage = (Stage) table.getScene().getWindow();
        ViewHelper.show(stage, "/view/LoginView.fxml", "Connexion - Library EMSI");
    }
    
    /**
     * Affiche une alerte
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}