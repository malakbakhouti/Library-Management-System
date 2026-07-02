package service;

import dao.UserDAO;
import model.UserAccount;
import java.util.List;

/**
 * Logique métier pour les utilisateurs.
 */
public class UserService {
    private final UserDAO dao = new UserDAO();
    
    /**
     * Authentifier un utilisateur
     */
    public UserAccount authenticate(String email, String password) {
        UserAccount user = dao.getByEmail(email);
        if (user == null) {
            return null;
        }
        // Comparaison simple du mot de passe
        // TODO: Ajouter le hashing BCrypt pour la production
        if (password.equals(user.getPassword())) {
            return user;
        }
        return null;
    }
    
    /**
     * Obtenir tous les utilisateurs
     */
    public List<UserAccount> getAll() {
        return dao.getAll();
    }
    
    /**
     * Obtenir un utilisateur par ID
     */
    public UserAccount getById(int id) {
        return dao.getById(id);
    }
    
    /**
     * Obtenir un utilisateur par email
     */
    public UserAccount getByEmail(String email) {
        return dao.getByEmail(email);
    }
    
    /**
     * Insérer un nouvel utilisateur
     * ✅ CORRECTION : Retourne boolean au lieu de int
     */
    public boolean insert(UserAccount user) {
        return dao.insert(user);
    }
    
    /**
     * Mettre à jour un utilisateur
     */
    public boolean update(UserAccount user) {
        return dao.update(user);
    }
    
    /**
     * Supprimer un utilisateur
     */
    public boolean delete(int id) {
        return dao.delete(id);
    }
}