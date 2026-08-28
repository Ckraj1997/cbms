package mca.fincorebanking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mca.fincorebanking.entity.KycDocument;
import mca.fincorebanking.entity.User;

public interface KycRepository extends JpaRepository<KycDocument, Long> {

    Optional<KycDocument> findByUser(User user);

    List<KycDocument> findByStatus(String status);
}