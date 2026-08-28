package mca.fincorebanking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import mca.fincorebanking.entity.Account;
import mca.fincorebanking.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccount(Account account);

    List<Transaction> findByAccountAndTransactionTimeBetween(
            Account account,
            LocalDateTime start,
            LocalDateTime end);

    long countByAccountAndTransactionTimeAfter(
            Account account,
            LocalDateTime time);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.account.user.username = :username
            ORDER BY t.transactionTime DESC
            """)
    List<Transaction> findTopNByUserOrderByTransactionTimeDesc(
            @Param("username") String username,
            Pageable pageable);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.account.id = :accountId
            AND (:type IS NULL OR t.type = :type)
            AND (:fromDate IS NULL OR t.transactionTime >= :fromDate)
            AND (:toDate IS NULL OR t.transactionTime <= :toDate)
            ORDER BY t.transactionTime DESC
            """)
    Page<Transaction> findTransactions(
            @Param("accountId") Long accountId,
            @Param("type") String type,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.account.id = :accountId
            AND (:type IS NULL OR t.type = :type)
            AND (:fromDate IS NULL OR t.transactionTime >= :fromDate)
            AND (:toDate IS NULL OR t.transactionTime <= :toDate)
            ORDER BY t.transactionTime DESC
            """)
    List<Transaction> findAllForExport(
            @Param("accountId") Long accountId,
            @Param("type") String type,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query("""
            SELECT COUNT(t)
            FROM Transaction t
            WHERE t.account.user.username = :username
            """)
    long countByUserUsername(@Param("username") String username);

    List<Transaction> findTop5ByAccountIdOrderByTransactionTimeDesc(Long accountId);

}
