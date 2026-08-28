package mca.fincorebanking.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class InterestConfig {

    public static double savingsInterestRate() {
        return NumericConfigStore.get().interest.savingsAnnualRate;
    }
}
