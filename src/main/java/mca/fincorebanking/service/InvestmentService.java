package mca.fincorebanking.service;

import java.util.List;

import mca.fincorebanking.entity.FixedDeposit;

public interface InvestmentService {

    FixedDeposit createFixedDeposit(Long userId, String accountNumber, Double amount, Integer tenureMonths);

    List<FixedDeposit> getUserInvestments(String username);

    FixedDeposit getInvestmentDetails(Long fdId);
}