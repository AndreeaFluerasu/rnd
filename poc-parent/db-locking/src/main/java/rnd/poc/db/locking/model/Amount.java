package rnd.poc.db.locking.model;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Currency;

import static java.lang.String.format;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
public class Amount {

    @ToString.Include
    private BigDecimal value;

    @ToString.Include
    private Currency currency;

    public static Amount of(BigDecimal value, Currency currency) {
        return new Amount(value, currency);
    }

    public void subtractAmount(Amount amount) {
        if (!amount.getCurrency().equals(this.currency)) {
            throw new IllegalArgumentException(format("Subtracted amount currency %s does not match account currency %s", amount, this));
        }
        this.value = this.value.subtract(amount.getValue());
        System.out.println("olaSUB: " + this.value);
    }

    public void addAmount(Amount amount) {
        if (!amount.getCurrency().equals(this.currency)) {
            throw new IllegalArgumentException(format("Amount currency [%s] does not match account currency [%s]", amount, this));
        }
        this.value = this.value.add(amount.getValue());
    }

    public Amount negate() {
        return Amount.of(this.value.negate(), this.currency);
    }

    public static Amount sum(Amount firstAmount, Amount secondAmount) {
        if (!firstAmount.getCurrency().equals(secondAmount.getCurrency())) {
            throw new IllegalArgumentException(format("Amount currencies don't match [%s, %s]", firstAmount, secondAmount));
        }
        return Amount.of(firstAmount.getValue().add(secondAmount.getValue()), firstAmount.getCurrency());
    }
}
