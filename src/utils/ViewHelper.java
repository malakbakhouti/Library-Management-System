package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class ViewHelper {
    
    /**
     * Charge une nouvelle vue FXML et l'affiche dans le stage donné
     */
    public static void show(Stage stage, String fxmlPath, String title) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║      ViewHelper.show()                 ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("📂 Chemin FXML: " + fxmlPath);
        System.out.println("📋 Titre: " + title);
        
        try {
            // Vérifier si la ressource existe
            URL fxmlUrl = ViewHelper.class.getResource(fxmlPath);
            System.out.println("🔍 Recherche de la ressource...");
            
            if (fxmlUrl == null) {
                System.err.println("❌ ERREUR CRITIQUE: Fichier FXML introuvable!");
                System.err.println("   Chemin recherché: " + fxmlPath);
                System.err.println("   Chemin absolu attendu: /view/LibrarianDashboard.fxml");
                System.err.println("\n📁 Vérifiez que le fichier existe dans:");
                System.err.println("   src/view/LibrarianDashboard.fxml");
                
                showError("Fichier introuvable", 
                         "Le fichier " + fxmlPath + " est introuvable.\n\n" +
                         "Vérifiez que le fichier existe dans:\n" +
                         "src/view/LibrarianDashboard.fxml");
                return;
            }
            
            System.out.println("✅ Ressource trouvée: " + fxmlUrl);
            System.out.println("⚙️  Chargement du FXML...");
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            System.out.println("✅ FXML chargé avec succès");
            System.out.println("⚙️  Création de la scene...");
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
            
            System.out.println("✅ Vue affichée avec succès!");
            System.out.println("════════════════════════════════════════\n");
            
        } catch (IOException e) {
            System.err.println("\n❌ ERREUR IOException lors du chargement:");
            System.err.println("   Fichier: " + fxmlPath);
            System.err.println("   Message: " + e.getMessage());
            System.err.println("\n📚 Stack trace complet:");
            e.printStackTrace();
            System.err.println("════════════════════════════════════════\n");
            
            showError("Erreur de chargement", 
                     "Impossible de charger la vue: " + fxmlPath + "\n\n" +
                     "Erreur: " + e.getMessage() + "\n\n" +
                     "Vérifiez la console pour plus de détails.");
                     
        } catch (Exception e) {
            System.err.println("\n❌ ERREUR INATTENDUE:");
            System.err.println("   Type: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            System.err.println("\n📚 Stack trace complet:");
            e.printStackTrace();
            System.out.println("════════════════════════════════════════\n");
            
            showError("Erreur inattendue", 
                     "Une erreur inattendue s'est produite:\n\n" +
                     e.getClass().getSimpleName() + ": " + e.getMessage() + "\n\n" +
                     "Vérifiez la console pour plus de détails.");
        }
    }
    
    /**
     * Crée une nouvelle fenêtre (Stage) et charge la vue dedans
     */
    public static Stage showInNewWindow(String fxmlPath, String title) {
        try {
            System.out.println("🪟 Ouverture d'une nouvelle fenêtre: " + fxmlPath);
            
            URL fxmlUrl = ViewHelper.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("❌ Fichier FXML introuvable: " + fxmlPath);
                showError("Fichier introuvable", "Le fichier " + fxmlPath + " est introuvable.");
                return null;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            Stage newStage = new Stage();
            Scene scene = new Scene(root);
            newStage.setScene(scene);
            newStage.setTitle(title);
            newStage.show();
            
            System.out.println("✅ Nouvelle fenêtre ouverte avec succès");
            
            return newStage;
            
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'ouverture de la fenêtre:");
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la fenêtre: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Ouvre une fenêtre modale (bloque la fenêtre parent)
     * CORRIGÉ: Teste plusieurs chemins possibles
     */
    public static Stage openModal(Stage parentStage, String fxmlPath, String title) {
        try {
            System.out.println("📋 Ouverture d'un modal: " + fxmlPath);
            
            // TESTER PLUSIEURS CHEMINS POSSIBLES
            String[] possiblePaths = {
                "view/" + fxmlPath,           // Sans / (pour resources/)
                "/view/" + fxmlPath,          // Avec / (pour src/)
                fxmlPath                      // Tel quel (si déjà complet)
            };
            
            URL fxmlUrl = null;
            String foundPath = null;
            
            for (String path : possiblePaths) {
                System.out.println("🔍 Test du chemin modal: " + path);
                
                if (path.startsWith("/")) {
                    fxmlUrl = ViewHelper.class.getResource(path);
                } else {
                    fxmlUrl = ViewHelper.class.getClassLoader().getResource(path);
                }
                
                System.out.println("   URL trouvée: " + fxmlUrl);
                
                if (fxmlUrl != null) {
                    foundPath = path;
                    System.out.println("✅ Chemin modal valide trouvé: " + path);
                    break;
                }
            }
            
            if (fxmlUrl == null) {
                System.err.println("❌ Fichier FXML introuvable après tous les tests: " + fxmlPath);
                System.err.println("   Chemins testés:");
                for (String path : possiblePaths) {
                    System.err.println("   - " + path);
                }
                showError("Fichier introuvable", 
                         "Le fichier " + fxmlPath + " est introuvable.\n\n" +
                         "Vérifiez que le fichier existe dans resources/view/");
                return null;
            }
            
            System.out.println("⚙️  Chargement du modal...");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            Stage modalStage = new Stage();
            Scene scene = new Scene(root);
            
            modalStage.setScene(scene);
            modalStage.setTitle(title);
            
            // Définir comme fenêtre modale
            if (parentStage != null) {
                modalStage.initModality(Modality.WINDOW_MODAL);
                modalStage.initOwner(parentStage);
            } else {
                modalStage.initModality(Modality.APPLICATION_MODAL);
            }
            
            System.out.println("✅ Modal prêt, affichage...");
            modalStage.showAndWait();
            
            System.out.println("✅ Modal fermé");
            
            return modalStage;
            
        } catch (IOException e) {
            System.err.println("❌ Erreur IOException lors de l'ouverture du modal:");
            System.err.println("   Fichier: " + fxmlPath);
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le modal: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de l'ouverture du modal:");
            System.err.println("   Type: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur inattendue", 
                     "Une erreur inattendue s'est produite:\n\n" +
                     e.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Affiche une alerte d'erreur
     */
    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}