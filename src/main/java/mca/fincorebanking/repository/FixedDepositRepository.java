package mca.fincorebanking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import mca.fincorebanking.entity.FixedDeposit;
import mca.fincorebanking.entity.User;

public interface FixedDepositRepository extends JpaRepository<FixedDeposit, Long> {

    List<FixedDeposit> findByUser(User user);

    List<FixedDeposit> findByStatus(String status);
}