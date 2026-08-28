package mca.fincorebanking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import mca.fincorebanking.entity.Loan;
import mca.fincorebanking.entity.User;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUser(User user);

    List<Loan> findByStatus(String status);

    long countByUserUsernameAndStatus(String username, String status);

    long countByStatus(String status);

    List<Loan> findByUserUsername(String username);

    @Modifying
    @Transactional
    @Query("UPDATE Loan a SET a.status = :status WHERE a.id = :loanId")
    int updateLoanStatusById(Long loanId, String status);

}
