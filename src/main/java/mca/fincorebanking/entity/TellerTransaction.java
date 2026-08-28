package mca.fincorebanking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "teller_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TellerTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private Double amount;
    private String status;

    @ManyToOne
    @JoinColumn(name = "teller_id")
    private User teller;

    @ManyToOne
    @JoinColumn(name = "customer_account_id")
    private Account targetAccount;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User approvedByManager;

    private LocalDateTime timestamp;
}