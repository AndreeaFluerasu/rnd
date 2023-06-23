package andreea.poc.db.locking.service;

import andreea.poc.db.locking.model.Amount;
import andreea.poc.db.locking.model.BankAccount;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Currency;

public class BankAccountFixture {

    @Builder(builderMethodName = "aBankAccount")
    private static BankAccount buildBankAccount(String iban, Amount balance) {
        return BankAccount.builder()
                .iban(iban)
                .holderName("John Doe")
                .balance(balance)
                .build();
    }


    public static class BankAccountBuilder {

        public BankAccountFixture.BankAccountBuilder iban(String iban) {
            this.iban = iban;
            return this;
        }

        public BankAccountFixture.BankAccountBuilder eurBalance(BigDecimal balance) {
            this.balance = Amount.of(balance, Currency.getInstance("EUR"));
            return this;
        }


    }


}
