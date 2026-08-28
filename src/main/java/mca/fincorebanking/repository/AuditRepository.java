package mca.fincorebanking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import mca.fincorebanking.entity.AuditLog;

public interface AuditRepository
                extends JpaRepository<AuditLog, Long> {
}
