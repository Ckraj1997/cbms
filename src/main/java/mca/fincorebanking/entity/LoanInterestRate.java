package mca.fincorebanking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_interest_rates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanInterestRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loanType;

    @Column(nullable = false)
    private Double interestRate;

    @Column(nullable = false)
    private boolean active = true;
}
