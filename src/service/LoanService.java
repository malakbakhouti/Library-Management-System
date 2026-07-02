package service;

import dao.LoanDAO;
import model.Loan;

import java.time.LocalDate;
import java.util.List;

public class LoanService {

    private final LoanDAO dao = new LoanDAO();

    public boolean insert(Loan l) { return dao.insert(l); }
    public boolean createLoanRequest(int userId, int bookId) { return dao.createLoanRequest(userId, bookId); }
    public List<Loan> getActiveLoans() { return dao.getActiveLoans(); }
    public List<Loan> findActiveLoans() { return dao.getActiveLoans(); }
    public boolean validateLoan(int loanId, int librarianId) { return dao.validateLoan(loanId, librarianId); }
    public boolean returnLoan(int loanId, LocalDate returnedDate, double penalty) { return dao.returnLoan(loanId, returnedDate, penalty); }
    public List<Loan> getLoansByUser(int userId) { return dao.getLoansByUser(userId); }
    public Loan getById(int id) { return dao.getById(id); }
    public List<Loan> getAll() { return dao.getAll(); }
}
