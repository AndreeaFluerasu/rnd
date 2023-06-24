package rnd.poc.db.locking.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AmountTest {

    @Test
    void addingPositiveAmount() {
        Amount amount = Amount.of(BigDecimal.ZERO, Currency.getInstance("EUR"));
        Amount positiveAmount = Amount.of(BigDecimal.valueOf(20), Currency.getInstance("EUR"));

        amount.addAmount(positiveAmount);

        assertThat(amount.getValue()).isEqualByComparingTo(BigDecimal.valueOf(20));
    }

    @Test
    void addingNegativeAmount() {
        Amount amount = Amount.of(BigDecimal.ZERO, Currency.getInstance("EUR"));
        Amount negativeAmount = Amount.of(BigDecimal.valueOf(-20), Currency.getInstance("EUR"));

        amount.addAmount(negativeAmount);

        assertThat(amount.getValue()).isEqualByComparingTo(BigDecimal.valueOf(-20));
    }

    @Test
    void addingAmountWithDifferentCurrency_shouldThrowException() {
        Amount eurAmount = Amount.of(BigDecimal.ZERO, Currency.getInstance("EUR"));
        Amount usdAmount = Amount.of(BigDecimal.valueOf(-20), Currency.getInstance("USD"));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> eurAmount.addAmount(usdAmount))
                .withMessage("Amount currency [Amount(value=-20, currency=USD)] does not match account currency [Amount(value=0, currency=EUR)]");
    }

    @Test
    void negateAmounts() {
        Amount zeroAmount = Amount.of(BigDecimal.ZERO, Currency.getInstance("EUR"));
        assertThat(zeroAmount.negate().getValue()).isEqualByComparingTo(BigDecimal.ZERO);

        Amount positiveAmount = Amount.of(BigDecimal.valueOf(20), Currency.getInstance("EUR"));
        assertThat(positiveAmount.negate().getValue()).isEqualByComparingTo(BigDecimal.valueOf(-20));

        Amount negativeAmount = Amount.of(BigDecimal.valueOf(-10), Currency.getInstance("EUR"));
        assertThat(negativeAmount.negate().getValue()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }

    @Test
    void addingTwoAmountsWithSameCurrency() {
        Amount firstAmount = Amount.of(BigDecimal.valueOf(30), Currency.getInstance("EUR"));
        Amount secondAmount = Amount.of(BigDecimal.valueOf(-50), Currency.getInstance("EUR"));

        assertThat(Amount.sum(firstAmount, secondAmount))
                .extracting(Amount::getValue, Amount::getCurrency)
                .containsExactly(BigDecimal.valueOf(-20), Currency.getInstance("EUR"));
    }

    @Test
    void addingTwoAmountsWithDifferentCurrency_shouldThrowException() {
        Amount firstAmount = Amount.of(BigDecimal.valueOf(60), Currency.getInstance("EUR"));
        Amount secondAmount = Amount.of(BigDecimal.valueOf(10), Currency.getInstance("USD"));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Amount.sum(firstAmount, secondAmount))
                .withMessage("Amount currencies don't match [Amount(value=60, currency=EUR), Amount(value=10, currency=USD)]");
    }
}