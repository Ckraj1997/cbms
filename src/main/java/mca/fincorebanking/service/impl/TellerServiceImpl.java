package mca.fincorebanking.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mca.fincorebanking.entity.Account;
import mca.fincorebanking.entity.TellerTransaction;
import mca.fincorebanking.entity.Transaction;
import mca.fincorebanking.entity.User;
import mca.fincorebanking.repository.AccountRepository;
import mca.fincorebanking.repository.TellerRepository;
import mca.fincorebanking.repository.TransactionRepository;
import mca.fincorebanking.repository.UserRepository;
import mca.fincorebanking.service.KycService;
import mca.fincorebanking.service.NotificationService;
import mca.fincorebanking.service.TellerDrawerService;
import mca.fincorebanking.service.TellerService;

@Service
public class TellerServiceImpl implements TellerService {

    private final AccountRepository accountRepository;
    private final TellerRepository tellerRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final KycService kycService;
    private final TellerDrawerService tellerDrawerService;
    private final NotificationService notificationService;

    public TellerServiceImpl(AccountRepository accountRepository,
            TellerRepository tellerRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            KycService kycService,
            TellerDrawerService tellerDrawerService,
            NotificationService notificationService
        ) {
        this.accountRepository = accountRepository;
        this.tellerRepository = tellerRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.kycService = kycService;
        this.tellerDrawerService = tellerDrawerService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public TellerTransaction depositCash(Long tellerId, String accountNumber, Double amount) {

        User teller = userRepository.findById(tellerId).orElseThrow(() -> new RuntimeException("Teller not found"));
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow();
        if (account == null)
            throw new RuntimeException("Invalid Account Number");
        validateTellerTransactionAccount(account);

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setAmount(amount);
        tx.setType("CASH DEPOSIT (Branch)");
        tx.setTransactionTime(LocalDateTime.now());
        tx.setBalanceAfter(account.getBalance());
        transactionRepository.save(tx);

        TellerTransaction tTx = new TellerTransaction();
        tTx.setTeller(teller);
        tTx.setTargetAccount(account);
        tTx.setAmount(amount);
        tTx.setType("CASH_DEPOSIT");
        tTx.setStatus("COMPLETED");
        tTx.setTimestamp(LocalDateTime.now());

        notificationService.notify(account.getUser(), "Your account " +accountNumber +"credit with Rs. "+ tx.getAmount() + "via branch deposit" );
        tellerDrawerService.processCash("DEPOSIT", amount);
        return tellerRepository.save(tTx);
    }

    @Override
    @Transactional
    public TellerTransaction withdrawCash(Long tellerId, String accountNumber, Double amount) {
        User teller = userRepository.findById(tellerId).orElseThrow(() -> new RuntimeException("Teller not found"));

        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow();
        if (account == null)
            throw new RuntimeException("Invalid Account Number");
        validateTellerTransactionAccount(account);

        if (account.getBalance() < amount) {
            throw new RuntimeException("Insufficient Funds");
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setAmount(amount);
        tx.setType("CASH WITHDRAWAL (Branch)");
        tx.setTransactionTime(LocalDateTime.now());
        tx.setBalanceAfter(account.getBalance());
        transactionRepository.save(tx);

        TellerTransaction tTx = new TellerTransaction();
        tTx.setTeller(teller);
        tTx.setTargetAccount(account);
        tTx.setAmount(amount);
        tTx.setType("CASH_WITHDRAWAL");
        tTx.setStatus("COMPLETED");
        tTx.setTimestamp(LocalDateTime.now());

        notificationService.notify(account.getUser(), "Your account " +accountNumber +"debitted with Rs. "+ tx.getAmount() + "via branch withdrawal" );
        tellerDrawerService.processCash("WITHDRAWAL", amount);
        return tellerRepository.save(tTx);
    }

    @Override
    public List<TellerTransaction> getTellerHistory(Long tellerId) {
        User teller = userRepository.findById(tellerId).orElse(null);
        return tellerRepository.findByTeller(teller);
    }

    private void validateTellerTransactionAccount(Account account) {
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException("Only active accounts can transact at the teller counter");
        }

        boolean kycVerified = kycService.getKycByUser(account.getUser())
                .map(t -> t.getStatus())
                .filter("VERIFIED"::equals)
                .isPresent();



        if (!kycVerified) {
            throw new RuntimeException(
                    "Customer KYC is not verified. Teller transactions are allowed only for KYC verified customers.");
        }
    }
}
