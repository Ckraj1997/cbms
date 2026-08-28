package mca.fincorebanking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import mca.fincorebanking.entity.LoanInterestRate;

public interface LoanInterestRateRepository
        extends JpaRepository<LoanInterestRate, Long> {

    LoanInterestRate findByLoanType(String loanType);

    List<LoanInterestRate> findByActiveTrue();
}
