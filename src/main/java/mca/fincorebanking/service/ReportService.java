package mca.fincorebanking.service;

import java.time.LocalDateTime;
import java.util.List;

import mca.fincorebanking.entity.Transaction;

public interface ReportService {

    List<Transaction> getAccountStatement(
            String username,
            String accountNumber,
            LocalDateTime from,
            LocalDateTime to);
}
