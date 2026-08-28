package mca.fincorebanking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import mca.fincorebanking.entity.TellerTransaction;
import mca.fincorebanking.entity.User;

public interface TellerRepository extends JpaRepository<TellerTransaction, Long> {
    List<TellerTransaction> findByTeller(User teller);
}