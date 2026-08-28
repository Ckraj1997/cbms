package mca.fincorebanking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import mca.fincorebanking.entity.FraudLog;

public interface FraudLogRepository
        extends JpaRepository<FraudLog, Long> {
}
