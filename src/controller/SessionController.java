package controller;

import model.UserAccount;

/**
 * Gestion de la session utilisateur
 * Stocke l'utilisateur courant et expose les utilitaires
 */
public class SessionController {

    private static UserAccount currentUser;

    /**
     * Définit l'utilisateur courant
     */
    public static void setCurrentUser(UserAccount user) {
        currentUser = user;
        System.out.println("✅ Session créée pour: " + (user != null ? user.getFullName() : "null"));
    }

    /**
     * Récupère l'utilisateur courant
     */
    public static UserAccount getCurrentUser() {
        return currentUser;
    }

    /**
     * Efface la session (méthode principale)
     */
    public static void clear() {
        if (currentUser != null) {
            System.out.println("🗑️  Session effacée pour: " + currentUser.getFullName());
        }
        currentUser = null;
    }

    /**
     * Alias pour clear() - pour compatibilité
     */
    public static void clearSession() {
        clear();
    }

    /**
     * Alias pour clear() - pour compatibilité
     */
    public static void logout() {
        clear();
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    public static boolean isAuthenticated() {
        return currentUser != null;
    }

    /**
     * Vérifie si l'utilisateur a un rôle spécifique
     * @param roleName "ADMIN", "LIBRARIAN", ou "MEMBER"
     */
    public static boolean hasRole(String roleName) {
        if (currentUser == null) return false;
        int id = currentUser.getRoleId();
        if (roleName == null) return false;
        
        switch (roleName.toUpperCase()) {
            case "ADMIN":     return id == 1;
            case "LIBRARIAN": return id == 2;
            case "MEMBER":    return id == 3;
            default:          return false;
        }
    }

    /**
     * Récupère l'ID du rôle de l'utilisateur courant
     */
    public static int getCurrentRoleId() {
        if (currentUser == null) return -1;
        return currentUser.getRoleId();
    }

    /**
     * Récupère le nom du rôle de l'utilisateur courant
     */
    public static String getCurrentRoleName() {
        if (currentUser == null) return "NONE";
        return currentUser.getRoleName();
    }

    /**
     * Récupère l'ID de l'utilisateur courant
     */
    public static int getCurrentUserId() {
        if (currentUser == null) return -1;
        return currentUser.getId();
    }

    /**
     * Récupère le nom complet de l'utilisateur courant
     */
    public static String getCurrentUserFullName() {
        if (currentUser == null) return "Invité";
        return currentUser.getFullName();
    }
}