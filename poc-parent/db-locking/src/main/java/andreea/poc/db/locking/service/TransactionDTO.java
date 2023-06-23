package andreea.poc.db.locking.service;

import java.math.BigDecimal;
import java.util.Currency;

public record TransactionDTO(
        DebitCreditIndicatorDTO debitCreditIndicator,
        String endToEndId,
        String account,
        String counterpartyAccount,
        BigDecimal amount,
        Currency currency
) {

    public enum DebitCreditIndicatorDTO {
        CREDIT,
        DEBIT
    }
}
