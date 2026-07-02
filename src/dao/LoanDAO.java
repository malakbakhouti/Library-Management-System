package dao;

import model.Loan;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {
    private Connection connection;

    public LoanDAO() {
        this.connection = DBConnection.getConnection();
    }

    // ========== MÉTHODES DE GESTION DES VALIDATIONS ==========
    
    /**
     * ✅ Récupère TOUS les emprunts pour l'écran de validation
     */
    public List<Loan> getAllLoansForValidation() {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT * FROM loans ORDER BY loan_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Loan loan = extractLoanFromResultSet(rs);
                loans.add(loan);
            }
            
            System.out.println("✅ Tous les emprunts chargés pour validation: " + loans.size());
        } catch (SQLException e) {
            System.err.println("❌ Erreur getAllLoansForValidation: " + e.getMessage());
            e.printStackTrace();
        }
        return loans;
    }
    
    /**
     * Compte le nombre d'emprunts en attente (non validés)
     */
    public int countPendingLoans() {
        String query = "SELECT COUNT(*) as total FROM loans WHERE validated_by IS NULL OR validated_by = 0";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur countPendingLoans: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Récupère tous les emprunts en attente de validation
     */
    public List<Loan> findPendingLoans() {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT * FROM loans WHERE validated_by IS NULL OR validated_by = 0 ORDER BY loan_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Loan loan = extractLoanFromResultSet(rs);
                loans.add(loan);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findPendingLoans: " + e.getMessage());
            e.printStackTrace();
        }
        return loans;
    }

    /**
     * ✅ Approuve un emprunt
     */
    public boolean approveLoan(int loanId, int librarianId) {
        String query = "UPDATE loans SET validated_by = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, librarianId);
            pstmt.setInt(2, loanId);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Emprunt #" + loanId + " approuvé par bibliothécaire #" + librarianId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur approveLoan: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ✅ Rejette un emprunt (validated_by = -1)
     */
    public boolean rejectLoan(int loanId, int librarianId) {
        String query = "UPDATE loans SET validated_by = -1 WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, loanId);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Emprunt #" + loanId + " rejeté par bibliothécaire #" + librarianId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur rejectLoan: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ✅ Marque un emprunt comme retourné avec calcul automatique de pénalité
     */
    public boolean markAsReturned(int loanId) {
        String checkSql = "SELECT return_due FROM loans WHERE id = ? AND returned = FALSE";
        String updateSql = "UPDATE loans SET returned = TRUE, returned_date = ?, penalty = ? WHERE id = ?";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql);
             PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
            
            // Vérifier la date de retour prévue
            checkStmt.setInt(1, loanId);
            ResultSet rs = checkStmt.executeQuery();
            
            double penalty = 0.0;
            if (rs.next()) {
                LocalDate returnDue = rs.getDate("return_due").toLocalDate();
                LocalDate today = LocalDate.now();
                
                // Calculer la pénalité : 5 MAD par jour de retard
                if (today.isAfter(returnDue)) {
                    long daysLate = ChronoUnit.DAYS.between(returnDue, today);
                    penalty = daysLate * 5.0;
                    System.out.println("⚠️ Retard de " + daysLate + " jours - Pénalité: " + penalty + " MAD");
                }
            }
            
            // Mettre à jour le statut
            updateStmt.setDate(1, Date.valueOf(LocalDate.now()));
            updateStmt.setDouble(2, penalty);
            updateStmt.setInt(3, loanId);
            
            int rows = updateStmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Emprunt #" + loanId + " marqué comme retourné");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur markAsReturned: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ========== MÉTHODES CRUD DE BASE ==========

    /**
     * Insérer un nouvel emprunt
     */
    public boolean insert(Loan loan) {
        String query = "INSERT INTO loans (user_id, book_id, loan_date, return_due, returned, validated_by, penalty) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, loan.getUserId());
            pstmt.setInt(2, loan.getBookId());
            pstmt.setDate(3, Date.valueOf(loan.getLoanDate()));
            pstmt.setDate(4, Date.valueOf(loan.getReturnDue()));
            pstmt.setBoolean(5, loan.isReturned());
            
            // Gérer validated_by NULL
            if (loan.getValidatedBy() == 0) {
                pstmt.setNull(6, Types.INTEGER);
            } else {
                pstmt.setInt(6, loan.getValidatedBy());
            }
            
            pstmt.setDouble(7, loan.getPenalty());
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    loan.setId(generatedKeys.getInt(1));
                }
                System.out.println("✅ Emprunt créé avec succès: ID=" + loan.getId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur insert: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Met à jour un emprunt complet
     */
    public boolean update(Loan loan) {
        String query = "UPDATE loans SET user_id = ?, book_id = ?, loan_date = ?, return_due = ?, " +
                      "returned = ?, returned_date = ?, validated_by = ?, penalty = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, loan.getUserId());
            pstmt.setInt(2, loan.getBookId());
            pstmt.setDate(3, Date.valueOf(loan.getLoanDate()));
            pstmt.setDate(4, Date.valueOf(loan.getReturnDue()));
            pstmt.setBoolean(5, loan.isReturned());
            
            // Gérer returned_date NULL
            if (loan.getReturnedDate() != null) {
                pstmt.setDate(6, Date.valueOf(loan.getReturnedDate()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }
            
            // Gérer validated_by NULL
            if (loan.getValidatedBy() == 0) {
                pstmt.setNull(7, Types.INTEGER);
            } else {
                pstmt.setInt(7, loan.getValidatedBy());
            }
            
            pstmt.setDouble(8, loan.getPenalty());
            pstmt.setInt(9, loan.getId());
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Emprunt mis à jour: ID=" + loan.getId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur update: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprime un emprunt
     */
    public boolean delete(int id) {
        String query = "DELETE FROM loans WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Emprunt supprimé: ID=" + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ========== MÉTHODES DE RÉCUPÉRATION ==========

    /**
     * Obtenir tous les emprunts
     */
    public List<Loan> getAll() {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT * FROM loans ORDER BY loan_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Loan loan = extractLoanFromResultSet(rs);
                loans.add(loan);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getAll: " + e.getMessage());
            e.printStackTrace();
        }
        return loans;
    }

    /**
     * Obtenir un emprunt par ID
     */
    public Loan findById(int id) {
        String query = "SELECT * FROM loans WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractLoanFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Alias pour findById (compatibilité)
     */
    public Loan getById(int id) {
        return findById(id);
    }

    /**
     * Obtenir les emprunts d'un utilisateur
     */
    public List<Loan> findByUserId(int userId) {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT * FROM loans WHERE user_id = ? ORDER BY loan_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Loan loan = extractLoanFromResultSet(rs);
                loans.add(loan);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findByUserId: " + e.getMessage());
            e.printStackTrace();
        }
        return loans;
    }

    /**
     * Alias pour findByUserId (compatibilité)
     */
    public List<Loan> getLoansByUser(int userId) {
        return findByUserId(userId);
    }

    // ========== MÉTHODES DE REQUÊTES SPÉCIALISÉES ==========

    /**
     * Obtenir les emprunts actifs (non retournés et validés)
     */
    public List<Loan> getActiveLoans() {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT * FROM loans WHERE returned = FALSE AND (validated_by IS NOT NULL AND validated_by > 0) ORDER BY return_due ASC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Loan loan = extractLoanFromResultSet(rs);
                loans.add(loan);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getActiveLoans: " + e.getMessage());
            e.printStackTrace();
        }
        return loans;
    }

    /**
     * Obtenir les emprunts en retard
     */
    public List<Loan> findOverdueLoans() {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT * FROM loans WHERE returned = FALSE AND return_due < CURDATE() ORDER BY return_due";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Loan loan = extractLoanFromResultSet(rs);
                loans.add(loan);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findOverdueLoans: " + e.getMessage());
            e.printStackTrace();
        }
        return loans;
    }

    // ========== MÉTHODES DE COMPTAGE ==========

    /**
     * Compter tous les emprunts
     */
    public int countAll() {
        String query = "SELECT COUNT(*) as total FROM loans";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur countAll: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Compter les emprunts actifs
     */
    public int countActiveLoans() {
        String query = "SELECT COUNT(*) as total FROM loans WHERE returned = FALSE AND (validated_by IS NOT NULL AND validated_by > 0)";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur countActiveLoans: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Compter les emprunts en retard
     */
    public int countOverdueLoans() {
        String query = "SELECT COUNT(*) as total FROM loans WHERE returned = FALSE AND return_due < CURDATE()";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur countOverdueLoans: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // ========== MÉTHODES DE GESTION DES DEMANDES ==========

    /**
     * Créer une demande d'emprunt (non validée)
     */
    public boolean createLoanRequest(int userId, int bookId) {
        String query = "INSERT INTO loans (user_id, book_id, loan_date, return_due, returned, validated_by, penalty) " +
                      "VALUES (?, ?, ?, ?, FALSE, NULL, 0.00)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            LocalDate loanDate = LocalDate.now();
            LocalDate returnDue = loanDate.plusDays(14); // 14 jours d'emprunt
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            pstmt.setDate(3, Date.valueOf(loanDate));
            pstmt.setDate(4, Date.valueOf(returnDue));
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Demande d'emprunt créée: User #" + userId + " - Book #" + bookId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur createLoanRequest: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Valider un emprunt (alias pour approveLoan)
     */
    public boolean validateLoan(int loanId, int librarianId) {
        return approveLoan(loanId, librarianId);
    }

    /**
     * Retourner un livre avec pénalité manuelle
     */
    public boolean returnLoan(int loanId, LocalDate returnDate, double penalty) {
        String query = "UPDATE loans SET returned = TRUE, returned_date = ?, penalty = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(returnDate));
            pstmt.setDouble(2, penalty);
            pstmt.setInt(3, loanId);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Livre retourné: Loan #" + loanId + " - Pénalité: " + penalty + " MAD");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur returnLoan: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Extrait un objet Loan depuis un ResultSet
     */
    private Loan extractLoanFromResultSet(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setId(rs.getInt("id"));
        loan.setUserId(rs.getInt("user_id"));
        loan.setBookId(rs.getInt("book_id"));
        loan.setLoanDate(rs.getDate("loan_date").toLocalDate());
        loan.setReturnDue(rs.getDate("return_due").toLocalDate());
        loan.setReturned(rs.getBoolean("returned"));
        
        // Gérer returned_date NULL
        Date returnedDate = rs.getDate("returned_date");
        if (returnedDate != null) {
            loan.setReturnedDate(returnedDate.toLocalDate());
        }
        
        loan.setValidatedBy(rs.getInt("validated_by"));
        loan.setPenalty(rs.getDouble("penalty"));
        
        return loan;
    }
}