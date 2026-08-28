package mca.fincorebanking.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import mca.fincorebanking.entity.AuditLog;
import mca.fincorebanking.repository.AuditRepository;
import mca.fincorebanking.service.AuditService;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;

    public AuditServiceImpl(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Override
    public void log(String username, String action) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setTimestamp(LocalDateTime.now());
        auditRepository.save(log);
    }

    @Override
    public List<AuditLog> getAllLogs() {
        return auditRepository.findAll();
    }
}
