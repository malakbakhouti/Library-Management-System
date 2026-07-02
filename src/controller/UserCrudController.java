package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.UserAccount;
import service.UserService;

import java.util.List;

public class UserCrudController {
    
    @FXML private TableView<UserAccount> table;
    @FXML private TableColumn<UserAccount, Integer> colId;
    @FXML private TableColumn<UserAccount, String> colName;
    @FXML private TableColumn<UserAccount, String> colEmail;
    @FXML private TableColumn<UserAccount, Integer> colRoleId;
    
    @FXML private TextField tfName;
    @FXML private TextField tfEmail;
    @FXML private PasswordField tfPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private Label lblMsg;
    
    private final UserService userService = new UserService();
    private final ObservableList<UserAccount> list = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // Initialisation des colonnes du tableau
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRoleId.setCellValueFactory(new PropertyValueFactory<>("roleId"));
        
        // Initialisation combobox roles
        cbRole.setItems(FXCollections.observableArrayList("ADMIN", "LIBRARIAN", "MEMBER"));
        
        // Charger les données
        loadTable();
        
        // Listener pour sélection dans le tableau
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                populateForm(newV);
            }
        });
    }
    
    /**
     * Charger la table avec tous les utilisateurs
     */
    private void loadTable() {
        list.clear();
        List<UserAccount> users = userService.getAll();
        list.addAll(users);
        table.setItems(list);
    }
    
    /**
     * Remplir le formulaire avec les données d'un utilisateur
     */
    private void populateForm(UserAccount u) {
        tfName.setText(u.getFullName());
        tfEmail.setText(u.getEmail());
        tfPassword.setText(u.getPassword());
        cbRole.getSelectionModel().select(roleIdToName(u.getRoleId()));
    }
    
    /**
     * Créer un nouvel utilisateur
     * ✅ CORRECTION : userService.insert() retourne maintenant boolean
     */
    @FXML
    private void onCreate() {
        // Validation
        if (tfName.getText().trim().isEmpty() || tfEmail.getText().trim().isEmpty() || tfPassword.getText().trim().isEmpty()) {
            lblMsg.setText("Veuillez remplir tous les champs.");
            return;
        }
        
        UserAccount u = new UserAccount();
        u.setFullName(tfName.getText().trim());
        u.setEmail(tfEmail.getText().trim());
        u.setPassword(tfPassword.getText().trim());
        u.setRoleId(roleNameToId(cbRole.getSelectionModel().getSelectedItem()));
        u.setAvatar(null);
        
        // ✅ CORRECTION : insert() retourne boolean maintenant
        boolean success = userService.insert(u);
        
        if (success) {
            lblMsg.setText("Utilisateur ajouté avec succès !");
            lblMsg.setStyle("-fx-text-fill: green;");
            loadTable();
            clearForm();
        } else {
            lblMsg.setText("Erreur lors de l'insertion.");
            lblMsg.setStyle("-fx-text-fill: red;");
        }
    }
    
    /**
     * Mettre à jour un utilisateur existant
     */
    @FXML
    private void onUpdate() {
        UserAccount sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMsg.setText("Sélectionner un utilisateur à modifier.");
            lblMsg.setStyle("-fx-text-fill: orange;");
            return;
        }
        
        // Validation
        if (tfName.getText().trim().isEmpty() || tfEmail.getText().trim().isEmpty()) {
            lblMsg.setText("Le nom et l'email sont requis.");
            return;
        }
        
        sel.setFullName(tfName.getText().trim());
        sel.setEmail(tfEmail.getText().trim());
        
        // Ne mettre à jour le mot de passe que s'il a été modifié
        if (!tfPassword.getText().trim().isEmpty()) {
            sel.setPassword(tfPassword.getText().trim());
        }
        
        sel.setRoleId(roleNameToId(cbRole.getSelectionModel().getSelectedItem()));
        
        boolean ok = userService.update(sel);
        
        if (ok) {
            lblMsg.setText("Utilisateur mis à jour avec succès !");
            lblMsg.setStyle("-fx-text-fill: green;");
            loadTable();
        } else {
            lblMsg.setText("Erreur lors de la mise à jour.");
            lblMsg.setStyle("-fx-text-fill: red;");
        }
    }
    
    /**
     * Supprimer un utilisateur
     */
    @FXML
    private void onDelete() {
        UserAccount sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMsg.setText("Sélectionner un utilisateur à supprimer.");
            lblMsg.setStyle("-fx-text-fill: orange;");
            return;
        }
        
        // Confirmation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer " + sel.getFullName() + " ?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean ok = userService.delete(sel.getId());
            
            if (ok) {
                lblMsg.setText("Utilisateur supprimé avec succès !");
                lblMsg.setStyle("-fx-text-fill: green;");
                loadTable();
                clearForm();
            } else {
                lblMsg.setText("Erreur lors de la suppression.");
                lblMsg.setStyle("-fx-text-fill: red;");
            }
        }
    }
    
    /**
     * Convertir un ID de rôle en nom
     */
    private String roleIdToName(int id) {
        switch (id) {
            case 1: return "ADMIN";
            case 2: return "LIBRARIAN";
            case 3: return "MEMBER";
            default: return "MEMBER";
        }
    }
    
    /**
     * Convertir un nom de rôle en ID
     */
    private int roleNameToId(String name) {
        if (name == null) return 3;
        switch (name.toUpperCase()) {
            case "ADMIN": return 1;
            case "LIBRARIAN": return 2;
            case "MEMBER": return 3;
            default: return 3;
        }
    }
    
    /**
     * Vider le formulaire
     */
    private void clearForm() {
        tfName.clear();
        tfEmail.clear();
        tfPassword.clear();
        cbRole.getSelectionModel().clearSelection();
        table.getSelectionModel().clearSelection();
        lblMsg.setText("");
    }
}