package mca.fincorebanking.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mca.fincorebanking.entity.Account;
import mca.fincorebanking.entity.Loan;
import mca.fincorebanking.entity.LoanInterestRate;
import mca.fincorebanking.entity.Transaction;
import mca.fincorebanking.entity.User;
import mca.fincorebanking.repository.AccountRepository;
import mca.fincorebanking.repository.LoanInterestRateRepository;
import mca.fincorebanking.repository.LoanRepository;
import mca.fincorebanking.repository.TransactionRepository;
import mca.fincorebanking.repository.UserRepository;
import mca.fincorebanking.service.KycService;
import mca.fincorebanking.service.LoanService;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final LoanInterestRateRepository loanInterestRateRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final KycService kycService;

    public LoanServiceImpl(LoanRepository loanRepository, UserRepository userRepository,
            LoanInterestRateRepository loanInterestRateRepository, AccountRepository accountRepository,
            TransactionRepository transactionRepository, KycService kycService) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.loanInterestRateRepository = loanInterestRateRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.kycService = kycService;
    }

    @Override
    @Transactional
    public void applyLoan(String username,
            Long accountId,
            String loanType,
            Double amount,
            Integer tenureMonths) {

        User user = userRepository.findByUsername(username).orElseThrow();

        boolean kycVerified = kycService.getKycByUser(user)
                .map(t -> t.getStatus())
                .filter("VERIFIED"::equals)
                .isPresent();

        if (!kycVerified) {
            throw new RuntimeException("KYC is not verified. Please complete your KYC to apply for a loan.");
        }

        LoanInterestRate cfg = loanInterestRateRepository.findByLoanType(loanType);

        if (!cfg.isActive()) {
            throw new RuntimeException("Loan type currently unavailable");
        }

        // Fetch the specific account
        Account targetAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!"ACTIVE".equals(targetAccount.getStatus())) {
            throw new RuntimeException("Cannot apply for a loan with an inactive account");
        }

        Loan loan = new Loan();
        loan.setLoanType(loanType);
        loan.setAmount(amount);
        loan.setTenureMonths(tenureMonths);

        // User user = userRepository.findByUsername(username).orElseThrow();

        loan.setInterestRate(cfg.getInterestRate());

        loan.setUser(user);
        loan.setAccount(targetAccount);
        loanRepository.save(loan);
    }

    @Override
    public List<Loan> getLoansByUser(String username) {
        return loanRepository.findByUserUsername(username);
    }

    @Override
    public List<Loan> getPendingLoans() {

        return loanRepository.findAll();
    }

    @Override
    @Transactional
    public void approveLoan(Long loanId) {
        Loan loan = findById(loanId);
        if ("APPROVED".equals(loan.getStatus())) {
            throw new RuntimeException("Loan is already approved and disbursed.");
        }
        loan.setStatus("APPROVED");
        loan.setDecisionDate(LocalDateTime.now());

        Account targetAccount = loan.getAccount();

        targetAccount.setBalance(targetAccount.getBalance() + loan.getAmount());
        accountRepository.save(targetAccount);

        Transaction tx = new Transaction();
        tx.setAccount(targetAccount);
        tx.setAmount(loan.getAmount());
        tx.setType("LOAN DISBURSEMENT");
        tx.setBalanceAfter(targetAccount.getBalance());
        tx.setTransactionTime(LocalDateTime.now());
        transactionRepository.save(tx);

        loanRepository.save(loan);
    }

    @Override
    public void rejectLoan(Long loanId) {
        Loan loan = findById(loanId);
        loan.setStatus("REJECTED");
        loan.setDecisionDate(LocalDateTime.now());
        loanRepository.save(loan);
    }

    @Override
    public Loan findById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
    }

    @Override
    public double calculateEmi(double amount, double annualRate, int tenureMonths) {

        double monthlyRate = annualRate / 12 / 100;
        double emi = (amount * monthlyRate * Math.pow(1 + monthlyRate, tenureMonths))
                / (Math.pow(1 + monthlyRate, tenureMonths) - 1);

        return Math.round(emi * 100.0) / 100.0;
    }

    @Override
    public long countActiveLoans(String username) {
        return loanRepository.countByUserUsernameAndStatus(username, "APPROVED");
    }

    @Override
    public long countByStatus(String status) {
        return loanRepository.countByStatus(status);
    }

    @Override
    public List<Loan> getLoansByStatus(String pending_admin) {
        return loanRepository.findByStatus(pending_admin);
    }

}
