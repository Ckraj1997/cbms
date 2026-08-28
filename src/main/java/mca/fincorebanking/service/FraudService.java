package mca.fincorebanking.service;

import java.util.List;

import mca.fincorebanking.entity.FraudLog;

public interface FraudService {

    void logFraud(String username, String reason);

    long countFrauds();

    List<FraudLog> getAllFraudLogs();

}
