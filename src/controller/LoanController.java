package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoanController {

    @FXML private TextField tfBookId;
    @FXML private Label lbMsg;

    @FXML
    public void initialize() {
        // Initialisation
    }

    @FXML
    private void onRequestLoan() {
        // Vérifier que l'utilisateur est un MEMBER
        if (!SessionController.hasRole("MEMBER")) {
            showAlert("Accès refusé", "Seuls les membres peuvent emprunter des livres.");
            return;
        }

        // Récupérer l'ID du livre
        String bookIdStr = tfBookId.getText().trim();
        if (bookIdStr.isEmpty()) {
            lbMsg.setText("Saisir l'ID du livre à emprunter.");
            return;
        }

        // TODO : appeler LoanService.createLoanRequest(userId, bookId)
        lbMsg.setText("Demande d'emprunt envoyée (fonctionnalité à implémenter).");
    }

    @FXML
    private void onValidateLoan() {
        // Vérifier les permissions
        if (!SessionController.hasRole("LIBRARIAN") && !SessionController.hasRole("ADMIN")) {
            showAlert("Accès refusé", "Seuls les librarians/admins peuvent valider des emprunts.");
            return;
        }

        // TODO : appeler LoanService.validateLoan(id)
        showAlert("Info", "Validation d'emprunt (fonctionnalité à implémenter).");
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}