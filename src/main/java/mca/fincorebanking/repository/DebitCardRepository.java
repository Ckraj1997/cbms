package mca.fincorebanking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import mca.fincorebanking.entity.Account;
import mca.fincorebanking.entity.DebitCard;

public interface DebitCardRepository extends JpaRepository<DebitCard, Long> {
    DebitCard findByAccount(Account account);
}