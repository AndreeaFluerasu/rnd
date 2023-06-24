package rnd.poc.db.locking.service;

import rnd.poc.db.locking.model.BankAccount;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public class TransactionDTOFixture {

    @Builder(builderMethodName = "aTransactionDTO")
    private static TransactionDTO buildTransactionDTO(TransactionDTO.DebitCreditIndicatorDTO debitCreditIndicator,
                                                      String account,
                                                      String counterpartyAccount,
                                                      BigDecimal amount,
                                                      Currency currency) {
        return new TransactionDTO(debitCreditIndicator,
                UUID.randomUUID().toString().replace("-", ""),
                account,
                "NL89RABO0316724692",
                amount,
                currency
        );
    }

    public static class TransactionDTOBuilder {

        public TransactionDTOBuilder credit() {
            this.debitCreditIndicator = TransactionDTO.DebitCreditIndicatorDTO.CREDIT;
            return this;
        }

        public TransactionDTOBuilder debit() {
            this.debitCreditIndicator = TransactionDTO.DebitCreditIndicatorDTO.DEBIT;
            return this;
        }

        public TransactionDTOBuilder eurAmount(BigDecimal amount) {
            this.amount = amount;
            this.currency = Currency.getInstance("EUR");
            return this;
        }

        public TransactionDTOBuilder account(BankAccount bankAccount) {
            this.account = bankAccount.getIban();
            return this;
        }
    }


}
