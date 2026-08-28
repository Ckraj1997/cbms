package mca.fincorebanking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import mca.fincorebanking.entity.Account;
import mca.fincorebanking.entity.ChequeBookRequest;

public interface ChequeBookRepository extends JpaRepository<ChequeBookRequest, Long> {
    List<ChequeBookRequest> findByAccount(Account account);

    List<ChequeBookRequest> findByStatus(String status);
}