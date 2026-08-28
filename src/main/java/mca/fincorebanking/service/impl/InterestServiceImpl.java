package mca.fincorebanking.service.impl;

import org.springframework.stereotype.Service;

import mca.fincorebanking.config.InterestConfig;
import mca.fincorebanking.config.NumericConfigStore;
import mca.fincorebanking.entity.Account;
import mca.fincorebanking.service.InterestService;

@Service
public class InterestServiceImpl implements InterestService {

    @Override
    public double calculateSavingsInterest(Account account, int months) {

        if (!"SAVINGS".equals(account.getAccountType())) {
            return NumericConfigStore.get().transactions.minimumAmount;
        }

        double principal = account.getBalance();
        double rate = InterestConfig.savingsInterestRate();
        double timeInYears = months / NumericConfigStore.get().interest.monthsPerYear;

        return (principal * rate * timeInYears) / NumericConfigStore.get().interest.percentageDivisor;
    }
}
