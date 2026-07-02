package dao;

import model.UserAccount;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private Connection connection;

    public UserDAO() {
        this.connection = DBConnection.getConnection();
        System.out.println("🔌 UserDAO: Connexion établie = " + (connection != null));
    }

    /**
     * Insérer un nouvel utilisateur
     * ✅ Retourne boolean
     */
    public boolean insert(UserAccount user) {
        String query = "INSERT INTO users (full_name, email, password, role_id, avatar, created_at) VALUES (?, ?, ?, ?, ?, NOW())";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.setInt(4, user.getRoleId());
            pstmt.setString(5, user.getAvatar() != null ? user.getAvatar() : "default.png");
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
                System.out.println("✅ Utilisateur créé: " + user.getEmail());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur insert: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtenir tous les utilisateurs
     */
    public List<UserAccount> getAll() {
        List<UserAccount> users = new ArrayList<>();
        String query = "SELECT u.*, r.name as role_name FROM users u " +
                       "LEFT JOIN roles r ON u.role_id = r.id " +
                       "ORDER BY u.id";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                UserAccount user = extractUserFromResultSet(rs);
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getAll: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    /**
     * ✅ Obtenir un utilisateur par ID
     * Utilisé par LibrarianDashboardController
     */
    public UserAccount getById(int id) {
        System.out.println("\n🔍 UserDAO.getById(" + id + ")");
        String query = "SELECT u.*, r.name as role_name FROM users u " +
                       "LEFT JOIN roles r ON u.role_id = r.id " +
                       "WHERE u.id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                UserAccount user = extractUserFromResultSet(rs);
                System.out.println("✅ Utilisateur trouvé: " + user.getFullName());
                return user;
            } else {
                System.out.println("❌ Aucun utilisateur avec l'ID " + id);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL getById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtenir un utilisateur par email
     * 🔥 MÉTHODE CRITIQUE POUR LE LOGIN
     */
    public UserAccount getByEmail(String email) {
        System.out.println("\n========================================");
        System.out.println("🔍 UserDAO.getByEmail()");
        System.out.println("========================================");
        System.out.println("📧 Email recherché: " + email);
        System.out.println("🔌 Connexion active: " + (connection != null));
        
        String query = "SELECT u.*, r.name as role_name FROM users u " +
                       "LEFT JOIN roles r ON u.role_id = r.id " +
                       "WHERE u.email = ?";
        System.out.println("📝 Requête SQL: " + query);
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, email);
            System.out.println("⚙️  Exécution de la requête...");
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("✅ UTILISATEUR TROUVÉ dans la BD!");
                UserAccount user = extractUserFromResultSet(rs);
                
                // Affichage détaillé
                System.out.println("┌─────────────────────────────────────┐");
                System.out.println("│  DÉTAILS DE L'UTILISATEUR           │");
                System.out.println("├─────────────────────────────────────┤");
                System.out.println("│ ID         : " + user.getId());
                System.out.println("│ Nom        : " + user.getFullName());
                System.out.println("│ Email      : " + user.getEmail());
                System.out.println("│ Role ID    : " + user.getRoleId());
                System.out.println("│ Role Name  : " + user.getRoleName());
                System.out.println("│ Password   : " + user.getPassword());
                System.out.println("│ Avatar     : " + user.getAvatar());
                System.out.println("└─────────────────────────────────────┘");
                System.out.println("========================================\n");
                
                return user;
            } else {
                System.out.println("❌ AUCUN RÉSULTAT dans la BD");
                System.out.println("   Vérifiez que l'email existe dans la table 'users'");
                System.out.println("========================================\n");
            }
        } catch (SQLException e) {
            System.err.println("\n❌ ERREUR SQL dans getByEmail:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Code: " + e.getErrorCode());
            System.err.println("   État SQL: " + e.getSQLState());
            e.printStackTrace();
            System.out.println("========================================\n");
        }
        return null;
    }

    /**
     * Mettre à jour un utilisateur
     */
    public boolean update(UserAccount user) {
        String query = "UPDATE users SET full_name = ?, email = ?, role_id = ?, avatar = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setInt(3, user.getRoleId());
            pstmt.setString(4, user.getAvatar());
            pstmt.setInt(5, user.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Utilisateur mis à jour: " + user.getEmail());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur update: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprimer un utilisateur
     */
    public boolean delete(int id) {
        String query = "DELETE FROM users WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Utilisateur supprimé: ID #" + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Authentifier un utilisateur
     */
    public UserAccount authenticate(String email, String password) {
        System.out.println("\n🔐 UserDAO.authenticate()");
        System.out.println("   Email: " + email);
        System.out.println("   Password: " + password);
        
        String query = "SELECT u.*, r.name as role_name FROM users u " +
                       "LEFT JOIN roles r ON u.role_id = r.id " +
                       "WHERE u.email = ? AND u.password = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                UserAccount user = extractUserFromResultSet(rs);
                System.out.println("✅ Authentification réussie: " + user.getFullName());
                return user;
            } else {
                System.out.println("❌ Authentification échouée");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur authenticate: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Vérifie si un email existe déjà
     */
    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur emailExists: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Compte le nombre total d'utilisateurs
     */
    public int countAll() {
        String query = "SELECT COUNT(*) FROM users";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur countAll: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Compte le nombre de membres (role_id = 2)
     */
    public int countMembers() {
        String query = "SELECT COUNT(*) FROM users WHERE role_id = 2";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur countMembers: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Récupère tous les membres (role_id = 2)
     */
    public List<UserAccount> getAllMembers() {
        List<UserAccount> members = new ArrayList<>();
        String query = "SELECT u.*, r.name as role_name FROM users u " +
                       "LEFT JOIN roles r ON u.role_id = r.id " +
                       "WHERE u.role_id = 2 " +
                       "ORDER BY u.full_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                members.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getAllMembers: " + e.getMessage());
            e.printStackTrace();
        }
        return members;
    }

    /**
     * ✅ Méthode utilitaire pour extraire un UserAccount du ResultSet
     * Inclut maintenant le role_name depuis la jointure
     */
    private UserAccount extractUserFromResultSet(ResultSet rs) throws SQLException {
        UserAccount user = new UserAccount();
        user.setId(rs.getInt("id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRoleId(rs.getInt("role_id"));
        user.setAvatar(rs.getString("avatar"));
        
        // ✅ Récupérer le nom du rôle depuis la jointure
        try {
            String roleName = rs.getString("role_name");
            if (roleName != null) {
                user.setRoleName(roleName);
            }
        } catch (SQLException e) {
            // Si role_name n'existe pas dans le ResultSet, on l'ignore
            System.out.println("⚠️ role_name non disponible dans cette requête");
        }
        
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }
        
        return user;
    }
}