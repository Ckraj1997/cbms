package mca.fincorebanking.service;

import mca.fincorebanking.entity.Account;

public interface InterestService {

    double calculateSavingsInterest(Account account, int months);
}
