package mca.fincorebanking.service;

import java.util.List;

import mca.fincorebanking.entity.AuditLog;

public interface AuditService {

    void log(String username, String action);

    List<AuditLog> getAllLogs();
}
