package mca.fincorebanking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import mca.fincorebanking.dto.TransactionReceiptDTO;
import mca.fincorebanking.entity.Transaction;

public interface TransactionService {

        List<Transaction> findRecentByUser(String username, int limit);

        Page<Transaction> getTransactions(
                        Long accountId,
                        String type,
                        LocalDateTime fromDate,
                        LocalDateTime toDate,
                        int page,
                        int size);

        List<Transaction> getAllTransactions(
                        Long accountId,
                        String type,
                        LocalDateTime fromDate,
                        LocalDateTime toDate);

        long countByUsername(String username);

        TransactionReceiptDTO transfer(
                        String username,
                        Long accountId,
                        Long beneficiaryId,
                        Double amount);

        Transaction findById(Long id);

        void processSelfTransaction(
                        String username,
                        Long accountId,
                        String type,
                        Double amount);

        List<Transaction> getAllTransactions();

}
