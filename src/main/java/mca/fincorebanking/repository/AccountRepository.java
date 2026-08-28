package mca.fincorebanking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import mca.fincorebanking.entity.Account;
import mca.fincorebanking.entity.User;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUser(User user);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUserUsername(String username);

    List<Account> findByUserUsernameAndStatus(String username, String status);

    long countByUserUsername(String username);

    @Query("""
            SELECT COALESCE(SUM(a.balance), 0)
            FROM Account a
            WHERE a.user.username = :username
            """)
    Double sumBalanceByUsername(@Param("username") String username);

    long countByStatus(String status);

    List<Account> findByStatus(String string);

    @Transactional
    @Modifying
    @Query("UPDATE Account a SET a.status = :status WHERE a.id = :id")
    int updateAccountStatusById(Long id, String status);

    @Query("SELECT a.user FROM Account a WHERE a.id = :accountId")
    Optional<User> findUserByAccountId(Long accountId);

}
