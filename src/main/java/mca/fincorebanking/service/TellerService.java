package mca.fincorebanking.service;

import java.util.List;

import mca.fincorebanking.entity.TellerTransaction;

public interface TellerService {
    TellerTransaction depositCash(Long tellerId, String accountNumber, Double amount);

    TellerTransaction withdrawCash(Long tellerId, String accountNumber, Double amount);

    List<TellerTransaction> getTellerHistory(Long tellerId);
}