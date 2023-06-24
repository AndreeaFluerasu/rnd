package rnd.poc.db.locking.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "transactions")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Transaction {

    public enum DebitCreditIndicator {
        DEBIT,
        CREDIT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DebitCreditIndicator debitCreditIndicator;

    private String endToEndId;

    @ManyToOne
    @JoinColumn(name = "bank_account_id")
    private BankAccount bankAccount;

    private String counterpartyAccount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "transaction_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "transaction_currency"))
    })
    private Amount transactionAmount;
    private Instant creationTimestamp;

    @PrePersist
    public void setCreationTimestamp() {
        this.creationTimestamp = Instant.now();
    }

    public Amount getSignedAmount() {
        return switch (debitCreditIndicator) {
            case CREDIT -> transactionAmount;
            case DEBIT -> transactionAmount.negate();
            default -> throw new IllegalArgumentException("Unknown debitCreditIndicator " + debitCreditIndicator);
        };
    }
}
