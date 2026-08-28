package mca.fincorebanking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Random;

@Entity
@Table(name = "debit_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DebitCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Column(nullable = false)
    private Integer cvv;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private String status;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @PrePersist
    public void generateCardDetails() {

        Random rand = new Random();
        this.cardNumber = "4590 " + (1000 + rand.nextInt(9000)) + " " + (1000 + rand.nextInt(9000)) + " "
                + (1000 + rand.nextInt(9000));
        this.cvv = 100 + rand.nextInt(900);
        this.expiryDate = LocalDate.now().plusYears(5);
        this.status = "ACTIVE";
    }
}