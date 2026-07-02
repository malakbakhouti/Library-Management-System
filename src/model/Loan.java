package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Loan {
    private int id;
    private int userId;
    private int bookId;
    private LocalDate loanDate;
    private LocalDate returnDue;
    private boolean returned;
    private LocalDate returnedDate;
    private int validatedBy;
    private double penalty;

    // ========== CONSTRUCTEURS ==========
    
    /**
     * Constructeur vide
     */
    public Loan() {
        this.returned = false;
        this.validatedBy = 0;
        this.penalty = 0.0;
    }

    /**
     * Constructeur avec userId et bookId (pour créer une demande)
     */
    public Loan(int userId, int bookId, LocalDate loanDate, LocalDate returnDue) {
        this.userId = userId;
        this.bookId = bookId;
        this.loanDate = loanDate;
        this.returnDue = returnDue;
        this.returned = false;
        this.validatedBy = 0;
        this.penalty = 0.0;
    }

    /**
     * Constructeur complet
     */
    public Loan(int id, int userId, int bookId, LocalDate loanDate, LocalDate returnDue,
                boolean returned, LocalDate returnedDate, int validatedBy, double penalty) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.loanDate = loanDate;
        this.returnDue = returnDue;
        this.returned = returned;
        this.returnedDate = returnedDate;
        this.validatedBy = validatedBy;
        this.penalty = penalty;
    }

    // ========== GETTERS ET SETTERS ==========
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getReturnDue() {
        return returnDue;
    }

    public void setReturnDue(LocalDate returnDue) {
        this.returnDue = returnDue;
    }

    public boolean isReturned() {
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }

    /**
     * ✅ Alias pour compatibilité avec MemberDashboardController
     */
    public LocalDate getActualReturnDate() {
        return returnedDate;
    }

    /**
     * ✅ Alias pour compatibilité avec MemberDashboardController
     */
    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.returnedDate = actualReturnDate;
    }

    public int getValidatedBy() {
        return validatedBy;
    }

    public void setValidatedBy(int validatedBy) {
        this.validatedBy = validatedBy;
    }

    public double getPenalty() {
        return penalty;
    }

    public void setPenalty(double penalty) {
        this.penalty = penalty;
    }

    // ========== MÉTHODES DE STATUT ==========
    
    /**
     * Retourne le statut de l'emprunt basé sur validatedBy
     */
    public String getStatus() {
        if (returned) {
            return "RETOURNÉ";
        } else if (validatedBy > 0) {
            return "APPROUVÉ";
        } else if (validatedBy == -1) {
            return "REJETÉ";
        } else {
            return "EN ATTENTE";
        }
    }

    /**
     * ✅ Vérifie si l'emprunt est validé (approuvé)
     * Nécessaire pour LibrarianDashboardController
     */
    public boolean isValidated() {
        return validatedBy > 0;
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Définit si l'emprunt est validé
     * Cette méthode est utilisée par MemberDashboardController
     * 
     * @param validated true pour valider (validatedBy = 1), false pour mettre en attente (validatedBy = 0)
     */
    public void setValidated(boolean validated) {
        if (validated) {
            // Si on valide et qu'aucun validateur n'est défini, on met 1 par défaut
            if (this.validatedBy == 0 || this.validatedBy == -1) {
                this.validatedBy = 1;
            }
        } else {
            // Si on invalide, on met en attente
            this.validatedBy = 0;
        }
    }

    /**
     * ✅ Alias pour isValidated - vérifie si approuvé
     */
    public boolean isApproved() {
        return validatedBy > 0;
    }

    /**
     * Vérifie si l'emprunt est en attente de validation
     */
    public boolean isPending() {
        return validatedBy == 0;
    }

    /**
     * Vérifie si l'emprunt est rejeté
     */
    public boolean isRejected() {
        return validatedBy == -1;
    }

    /**
     * Vérifie si l'emprunt est en retard
     */
    public boolean isOverdue() {
        return !returned && returnDue != null && returnDue.isBefore(LocalDate.now());
    }

    // ========== MÉTHODES DE CALCUL ==========
    
    /**
     * Calcule le nombre de jours de retard
     */
    public long getDaysOverdue() {
        if (!isOverdue()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(returnDue, LocalDate.now());
    }

    /**
     * Calcule le nombre de jours restants avant la date de retour
     */
    public long getDaysRemaining() {
        if (returned || returnDue == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), returnDue);
        return Math.max(0, days);
    }

    /**
     * Calcule automatiquement la pénalité (5 MAD par jour de retard)
     */
    public double calculatePenalty() {
        if (isOverdue()) {
            return getDaysOverdue() * 5.0;
        }
        return 0.0;
    }

    // ========== MÉTHODES UTILITAIRES POUR L'UI ==========
    
    /**
     * Retourne le statut de l'emprunt sous forme de texte formaté
     */
    public String getStatusText() {
        if (isRejected()) {
            return "❌ Rejeté";
        } else if (isPending()) {
            return "⏳ En attente";
        } else if (returned) {
            return "✅ Retourné";
        } else if (isOverdue()) {
            return "⚠️ En retard";
        } else if (isApproved()) {
            return "📖 Actif";
        }
        return "❓ Inconnu";
    }

    /**
     * Retourne la couleur associée au statut (pour l'UI JavaFX)
     */
    public String getStatusColor() {
        if (isRejected()) {
            return "#e53e3e"; // Rouge
        } else if (isPending()) {
            return "#ed8936"; // Orange
        } else if (returned) {
            return "#48bb78"; // Vert
        } else if (isOverdue()) {
            return "#e53e3e"; // Rouge
        } else if (isApproved()) {
            return "#4299e1"; // Bleu
        }
        return "#718096"; // Gris
    }

    /**
     * Retourne une description complète du statut
     */
    public String getStatusDescription() {
        if (isRejected()) {
            return "Demande refusée par le bibliothécaire";
        } else if (isPending()) {
            return "En attente de validation";
        } else if (returned && returnedDate != null) {
            return "Retourné le " + returnedDate.toString();
        } else if (isOverdue()) {
            long days = getDaysOverdue();
            return "Retard de " + days + " jour" + (days > 1 ? "s" : "");
        } else if (isApproved() && returnDue != null) {
            long days = getDaysRemaining();
            return "À retourner dans " + days + " jour" + (days > 1 ? "s" : "");
        }
        return "Statut inconnu";
    }

    /**
     * Vérifie si l'emprunt peut être retourné
     */
    public boolean canBeReturned() {
        return !returned && isApproved();
    }

    /**
     * Vérifie si l'emprunt peut être validé
     */
    public boolean canBeValidated() {
        return isPending() && !returned;
    }

    // ========== MÉTHODES D'AFFICHAGE ==========

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", loanDate=" + loanDate +
                ", returnDue=" + returnDue +
                ", returned=" + returned +
                ", returnedDate=" + returnedDate +
                ", validatedBy=" + validatedBy +
                ", penalty=" + penalty +
                ", status=" + getStatus() +
                '}';
    }

    /**
     * Affichage détaillé pour le débogage
     */
    public String toDetailedString() {
        return "Loan {\n" +
                "  ID: " + id + "\n" +
                "  User ID: " + userId + "\n" +
                "  Book ID: " + bookId + "\n" +
                "  Date emprunt: " + loanDate + "\n" +
                "  Date retour prévue: " + returnDue + "\n" +
                "  Retourné: " + (returned ? "Oui" : "Non") + "\n" +
                "  Date retour réelle: " + (returnedDate != null ? returnedDate : "N/A") + "\n" +
                "  Validé par: " + (validatedBy > 0 ? "Librarian #" + validatedBy : 
                                    validatedBy == -1 ? "Rejeté" : "En attente") + "\n" +
                "  Pénalité: " + penalty + " MAD\n" +
                "  Statut: " + getStatusText() + "\n" +
                "  Description: " + getStatusDescription() + "\n" +
                "}";
    }
}