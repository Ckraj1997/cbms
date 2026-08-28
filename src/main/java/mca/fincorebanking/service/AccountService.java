package mca.fincorebanking.service;
import java.util.List;

import mca.fincorebanking.entity.Account;
import mca.fincorebanking.entity.Notification;
import mca.fincorebanking.entity.Transaction;

public interface AccountService {
    List<Account> findByUsername(String username);

    List<Account> findActiveByUsername(String username);

    long countByUsername(String username);

    Double totalBalanceByUsername(String username);

    long countActiveAccounts();

    void requestAccount(Account account, String username);

    Account getAccountById(Long id);

    List<Transaction> getRecentTransactions(Long accountId);

    List<Notification> getAccountNotifications(Long accountId);

    List<Account> getPendingAccounts();

    void approveAccount(Long accountId);

    Account findByAccountNumber(String searchAccountNo);

    void updateAccountStatus(Long id, String pending_admin);

    List<Account> getAccountsByStatus(String pending_admin);

    void freezeAccountByUsername(String username);

}
