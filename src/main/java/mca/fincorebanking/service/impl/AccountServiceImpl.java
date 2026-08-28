package mca.fincorebanking.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import mca.fincorebanking.entity.Account;
import mca.fincorebanking.entity.Notification;
import mca.fincorebanking.entity.Transaction;
import mca.fincorebanking.entity.User;
import mca.fincorebanking.repository.AccountRepository;
import mca.fincorebanking.repository.NotificationRepository;
import mca.fincorebanking.repository.TransactionRepository;
import mca.fincorebanking.repository.UserRepository;
import mca.fincorebanking.service.AccountService;
import mca.fincorebanking.service.NotificationService;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public AccountServiceImpl(AccountRepository accountRepository, UserRepository userRepository,
            TransactionRepository transactionRepository,
            NotificationRepository notificationRepository,
        NotificationService notificationService) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }
    private String generateAccountNumber() {

        return "AC" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public List<Account> findByUsername(String username) {
        return accountRepository.findByUserUsername(username);
    }

    @Override
    public List<Account> findActiveByUsername(String username) {
        return accountRepository.findByUserUsernameAndStatus(username, "ACTIVE");
    }

    @Override
    public long countByUsername(String username) {
        return accountRepository.countByUserUsername(username);
    }

    @Override
    public Double totalBalanceByUsername(String username) {
        return accountRepository.sumBalanceByUsername(username);
    }

    @Override
    public long countActiveAccounts() {
        return accountRepository.countByStatus("ACTIVE");
    }

    @Override
    public void requestAccount(Account account, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        account.setUser(user);
        account.setStatus("PENDING");
        account.setAccountNumber(generateAccountNumber());

        if (account.getBalance() == null) {
            account.setBalance(0.0);
        }

        notificationService.notify(user, "Account created with account number " + account.getAccountNumber() + "and sent to manager for approval");

        accountRepository.save(account);
    }

    @Override
    public List<Account> getPendingAccounts() {
        return accountRepository.findByStatus("PENDING");
    }

    @Override
    public void approveAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow();

        account.setStatus("ACTIVE");
        Optional<User> user = accountRepository.findUserByAccountId(accountId);

        notificationService.notify(user.get(), "Your account has bee approve " + account.toString());
        accountRepository.save(account);
    }

    @Override
    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @Override
    public List<Transaction> getRecentTransactions(Long accountId) {
        return transactionRepository
                .findTop5ByAccountIdOrderByTransactionTimeDesc(accountId);
    }

    @Override
    public List<Notification> getAccountNotifications(Long accountId) {
        return notificationRepository.findByAccount_Id(accountId);
    }

    @Override
    public Account findByAccountNumber(String searchAccountNo) {
        return accountRepository.findByAccountNumber(searchAccountNo).orElseThrow();
    }

    @Override
    public void updateAccountStatus(Long id, String status) {
        int updated = accountRepository.updateAccountStatusById(id, status);
        if (updated == 0) {
            throw new RuntimeException("Account not found with id: " + id);
        }
    }

    @Override
    public List<Account> getAccountsByStatus(String pending_admin) {
        return accountRepository.findByStatus(pending_admin);
    }

    @Override
    public void freezeAccountByUsername(String username) {
        List<Account> accounts = accountRepository.findByUserUsername(username);
        if (accounts.isEmpty()) {
            throw new RuntimeException("No accounts found for user: " + username);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationService.notify(user, "Your Account " + accounts.toString() + " Has been blocked, contact branch for the details");

        for (Account acc : accounts) {
            acc.setStatus("FROZEN");
            accountRepository.save(acc);
        }
    }

}
