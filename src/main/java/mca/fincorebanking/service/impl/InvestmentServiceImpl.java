package mca.fincorebanking.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mca.fincorebanking.entity.Account;
import mca.fincorebanking.entity.FixedDeposit;
import mca.fincorebanking.entity.Transaction;
import mca.fincorebanking.entity.User;
import mca.fincorebanking.repository.AccountRepository;
import mca.fincorebanking.repository.FixedDepositRepository;
import mca.fincorebanking.repository.TransactionRepository;
import mca.fincorebanking.repository.UserRepository;
import mca.fincorebanking.service.InvestmentService;
import mca.fincorebanking.service.NotificationService;

@Service
public class InvestmentServiceImpl implements InvestmentService {

    private final FixedDepositRepository fdRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final NotificationService NotificationService;

    public InvestmentServiceImpl(FixedDepositRepository fdRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository, NotificationService notificationService) {
        this.fdRepository = fdRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.NotificationService = notificationService;
    }

    @Override
    @Transactional
    public FixedDeposit createFixedDeposit(Long userId, String accountNumber, Double amount, Integer tenureMonths) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow();
        if (account == null) {
            throw new RuntimeException("Account not found");
        }

        if (account.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds in linked account");
        }

        Double rate = (tenureMonths >= 12) ? 7.5 : 5.5;

        double r = rate / 100;
        int n = 4;
        double t = tenureMonths / 12.0;
        double maturityAmount = amount * Math.pow(1 + (r / n), n * t);

        double newBalance = account.getBalance() - amount;
        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction debitTx = new Transaction();
        debitTx.setAccount(account);
        debitTx.setAmount(amount);
        debitTx.setType("DEBIT (FD OPEN)");
        debitTx.setTransactionTime(LocalDateTime.now());
        debitTx.setBalanceAfter(newBalance);
        transactionRepository.save(debitTx);

        LocalDateTime ldt = LocalDateTime.now();
        FixedDeposit fd = new FixedDeposit();
        fd.setUser(user);
        fd.setLinkedAccount(account);
        fd.setPrincipalAmount(amount);
        fd.setInterestRate(rate);
        fd.setTenureMonths(tenureMonths);
        fd.setStartDate(ldt);
        fd.setStatus("ACTIVE");
        fd.setMaturityDate(ldt.plusMonths(tenureMonths));
        fd.setMaturityAmount(maturityAmount);

        NotificationService.notify(user, "You have successfully created a fixed deposit of " + fd.getPrincipalAmount() + " from " + fd.getStartDate().toString() + " for " + fd.getTenureMonths() + " months");

        return fdRepository.save(fd);
    }

    @Override
    public List<FixedDeposit> getUserInvestments(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();

        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return fdRepository.findByUser(user);
    }

    @Override
    public FixedDeposit getInvestmentDetails(Long fdId) {
        return fdRepository.findById(fdId)
                .orElseThrow(() -> new RuntimeException("Investment not found"));
    }
}