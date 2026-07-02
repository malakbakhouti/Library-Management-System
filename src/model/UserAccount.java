package model;

import java.time.LocalDateTime;

public class UserAccount {
    private int id;
    private String fullName;
    private String email;
    private String password;
    private int roleId;
    private String roleName;  // ✅ AJOUTÉ : pour stocker le nom du rôle depuis la BD
    private String avatar;
    private LocalDateTime createdAt;

    // Constructeur vide
    public UserAccount() {
    }

    // Constructeur complet
    public UserAccount(int id, String fullName, String email, String password, int roleId, String avatar, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.roleId = roleId;
        this.avatar = avatar;
        this.createdAt = createdAt;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ✅ MODIFIÉ : getRoleName() retourne roleName si disponible, sinon calcule selon roleId
    public String getRoleName() {
        // Si roleName est défini depuis la BD, on l'utilise
        if (roleName != null && !roleName.isEmpty()) {
            return roleName;
        }
        
        // Sinon, on le calcule selon roleId (fallback)
        switch (roleId) {
            case 1:
                return "ADMIN";
            case 2:
                return "LIBRARIAN";
            case 3:
                return "MEMBER";
            default:
                return "UNKNOWN";
        }
    }

    // ✅ AJOUTÉ : setter pour roleName
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    // ✅ AJOUTÉ : Méthode utilitaire pour obtenir le rôle sous forme de texte
    public String getRole() {
        return getRoleName();
    }

    // ✅ AJOUTÉ : Méthodes de vérification de rôle
    public boolean isAdmin() {
        return roleId == 1 || "ADMIN".equalsIgnoreCase(roleName);
    }

    public boolean isLibrarian() {
        return roleId == 2 || "LIBRARIAN".equalsIgnoreCase(roleName);
    }

    public boolean isMember() {
        return roleId == 3 || "MEMBER".equalsIgnoreCase(roleName);
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", roleId=" + roleId +
                ", roleName='" + getRoleName() + '\'' +
                ", avatar='" + avatar + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}