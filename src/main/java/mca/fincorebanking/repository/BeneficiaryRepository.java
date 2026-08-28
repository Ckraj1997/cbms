package mca.fincorebanking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import mca.fincorebanking.entity.Beneficiary;
import mca.fincorebanking.entity.User;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByUser(User user);

    List<Beneficiary> findByStatus(String status);

    List<Beneficiary> findByUserAndStatus(User user, String status);

    long countByStatus(String pending);

    List<Beneficiary> findByUserUsernameAndStatus(String username, String string);

}
