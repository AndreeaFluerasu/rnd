package rnd.poc.db.locking.service;

import rnd.poc.db.locking.model.Amount;
import rnd.poc.db.locking.model.BankAccount;
import rnd.poc.db.locking.model.BankAccountRepository;
import rnd.poc.db.locking.model.Transaction;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TransactionDTOMapper {

    private BankAccountRepository bankAccountRepository;

    public Transaction toEntity(TransactionDTO transactionDTO) {
        BankAccount bankAccount = bankAccountRepository.findByIban(transactionDTO.account())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        return Transaction.builder()
                .endToEndId(transactionDTO.endToEndId())
                .bankAccount(bankAccount)
                .counterpartyAccount(transactionDTO.counterpartyAccount())
                .transactionAmount(Amount.of(transactionDTO.amount(), transactionDTO.currency()))
                .debitCreditIndicator(Transaction.DebitCreditIndicator.valueOf(transactionDTO.debitCreditIndicator().name()))
                .build();
    }
}
