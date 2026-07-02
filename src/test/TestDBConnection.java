package test;

import dao.DBConnection;
import dao.UserDAO;
import model.UserAccount;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDBConnection {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   TEST DE CONNEXION À LA BASE");
        System.out.println("========================================\n");
        
        // TEST 1: Connexion directe
        testDirectConnection();
        
        // TEST 2: UserDAO
        testUserDAO();
        
        System.out.println("\n========================================");
        System.out.println("   FIN DES TESTS");
        System.out.println("========================================");
    }
    
    private static void testDirectConnection() {
        System.out.println("📌 TEST 1: Connexion directe à la BD");
        System.out.println("─────────────────────────────────────");
        
        try {
            Connection conn = DBConnection.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connexion établie avec succès!");
                System.out.println("   URL: " + conn.getMetaData().getURL());
                System.out.println("   User: " + conn.getMetaData().getUserName());
                
                // Tester une requête simple
                String query = "SELECT * FROM users WHERE email = 'librarian@emsi.local'";
                System.out.println("\n📝 Exécution: " + query);
                
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                
                if (rs.next()) {
                    System.out.println("\n✅ Résultat trouvé:");
                    System.out.println("   ID       : " + rs.getInt("id"));
                    System.out.println("   Nom      : " + rs.getString("full_name"));
                    System.out.println("   Email    : " + rs.getString("email"));
                    System.out.println("   Role ID  : " + rs.getInt("role_id"));
                    System.out.println("   Password : " + rs.getString("password"));
                } else {
                    System.out.println("❌ Aucun résultat trouvé");
                    System.out.println("   Vérifiez que l'utilisateur existe dans la table 'users'");
                }
                
                rs.close();
                stmt.close();
            } else {
                System.out.println("❌ Connexion échouée ou fermée");
            }
        } catch (Exception e) {
            System.err.println("\n❌ ERREUR CRITIQUE:");
            System.err.println("   Type: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            System.err.println("\n📚 Stack trace complet:");
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    private static void testUserDAO() {
        System.out.println("📌 TEST 2: UserDAO.getByEmail()");
        System.out.println("─────────────────────────────────────");
        
        try {
            UserDAO userDAO = new UserDAO();
            
            // Test 1: Email du librarian
            String email = "librarian@emsi.local";
            System.out.println("🔍 Test avec: " + email);
            
            UserAccount user = userDAO.getByEmail(email);
            
            if (user != null) {
                System.out.println("\n✅ SUCCÈS - Utilisateur trouvé:");
                System.out.println("   ID         : " + user.getId());
                System.out.println("   Nom        : " + user.getFullName());
                System.out.println("   Email      : " + user.getEmail());
                System.out.println("   Role ID    : " + user.getRoleId());
                System.out.println("   Role Name  : " + user.getRoleName());
                System.out.println("   Password   : " + user.getPassword());
                System.out.println("\n   toString(): " + user.toString());
            } else {
                System.out.println("\n❌ ÉCHEC - Utilisateur non trouvé");
                System.out.println("   L'email n'existe pas dans la BD");
            }
            
            // Test 2: Authentification
            System.out.println("\n\n🔐 TEST 3: Authentification complète");
            System.out.println("─────────────────────────────────────");
            UserAccount authUser = userDAO.authenticate("librarian@emsi.local", "lib123");
            
            if (authUser != null) {
                System.out.println("✅ Authentification réussie!");
                System.out.println("   Utilisateur: " + authUser.getFullName());
                System.out.println("   Rôle: " + authUser.getRoleName());
            } else {
                System.out.println("❌ Authentification échouée");
                System.out.println("   Email ou mot de passe incorrect");
            }
            
        } catch (Exception e) {
            System.err.println("\n❌ ERREUR dans testUserDAO:");
            System.err.println("   Type: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            System.err.println("\n📚 Stack trace complet:");
            e.printStackTrace();
        }
        
        System.out.println();
    }
}