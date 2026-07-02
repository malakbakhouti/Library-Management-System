package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import dao.UserDAO;
import model.UserAccount;
import utils.ViewHelper;

public class LoginController {

    // ======= FXML FIELDS =======
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    // ======= DAO =======
    private UserDAO userDAO;

    // ======= INITIALIZE =======
    @FXML
    public void initialize() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  LoginController - INITIALISATION     ║");
        System.out.println("╚════════════════════════════════════════╝");

        try {
            roleComboBox.getItems().addAll("ADMIN", "LIBRARIAN", "MEMBER");
            System.out.println("✅ ComboBox initialisé");

            userDAO = new UserDAO();
            System.out.println("✅ UserDAO créé");

            // ✅ DÉMARRER AVEC CHAMPS VIDES (pas de pré-remplissage)
            roleComboBox.getSelectionModel().clearSelection();
            emailField.clear();
            passwordField.clear();
            System.out.println("✅ Champs vides au démarrage");

        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de l'initialisation:");
            e.printStackTrace();
        }

        System.out.println("════════════════════════════════════════\n");
    }

    // ======= LOGIN ACTION =======
    @FXML
    private void onLogin(ActionEvent event) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     TENTATIVE DE CONNEXION             ║");
        System.out.println("╚════════════════════════════════════════╝");

        String role  = roleComboBox.getValue();
        String email = emailField.getText().trim();
        String pwd   = passwordField.getText().trim();

        System.out.println("📋 Rôle sélectionné : " + role);
        System.out.println("📧 Email            : " + email);
        System.out.println("🔒 Password         : " + pwd);

        // ---- Validation ----
        if (role == null || email.isEmpty() || pwd.isEmpty()) {
            System.out.println("❌ Champs vides");
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        // ---- Recherche utilisateur ----
        System.out.println("\n🔍 Recherche utilisateur...");
        UserAccount user = userDAO.getByEmail(email);

        if (user == null) {
            System.out.println("❌ Aucun utilisateur trouvé");
            showAlert("Erreur", "Email introuvable.");
            return;
        }

        System.out.println("✅ Utilisateur trouvé: " + user.getFullName());
        System.out.println("   Role ID : " + user.getRoleId());
        System.out.println("   Password: " + user.getPassword());

        // ---- Vérification mot de passe ----
        System.out.println("\n🔐 Vérification mot de passe...");
        if (!pwd.equals(user.getPassword())) {
            System.out.println("❌ Mot de passe incorrect");
            showAlert("Erreur", "Mot de passe incorrect.");
            return;
        }
        System.out.println("✅ Mot de passe correct");

        // ---- Vérification rôle ----
        System.out.println("\n🎭 Vérification rôle...");
        boolean roleMatch = false;
        if (user.getRoleId() == 1 && role.equals("ADMIN")) {
            roleMatch = true;
        } else if (user.getRoleId() == 2 && role.equals("LIBRARIAN")) {
            roleMatch = true;
        } else if (user.getRoleId() == 3 && role.equals("MEMBER")) {
            roleMatch = true;
        }

        if (!roleMatch) {
            System.out.println("❌ Rôle incorrect");
            showAlert("Erreur", "Rôle incorrect pour cet utilisateur.");
            return;
        }
        System.out.println("✅ Rôle correct");

        // ---- Sauvegarde session ----
        System.out.println("\n💾 Création session...");
        SessionController.setCurrentUser(user);
        System.out.println("✅ Session créée pour: " + user.getFullName());

        // ---- Redirection ----
        System.out.println("\n🚀 Redirection...");
        Stage stage = (Stage) emailField.getScene().getWindow();

        try {
            switch (user.getRoleId()) {
                case 1:
                    System.out.println("→ AdminDashboard");
                    ViewHelper.show(stage, "/view/AdminDashboard.fxml", "Admin Dashboard");
                    break;
                case 2:
                    System.out.println("→ LibrarianDashboard");
                    ViewHelper.show(stage, "/view/LibrarianDashboard.fxml", "Bibliothécaire - Dashboard");
                    break;
                case 3:
                    System.out.println("→ MemberDashboard");
                    ViewHelper.show(stage, "/view/MemberDashboard.fxml", "Membre - Dashboard");
                    break;
                default:
                    System.out.println("❌ Rôle inconnu: " + user.getRoleId());
                    showAlert("Erreur", "Rôle inconnu.");
            }
        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de la redirection:");
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement: " + e.getMessage());
        }

        System.out.println("════════════════════════════════════════\n");
    }

    // ======= ALERT UTILITY =======
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
