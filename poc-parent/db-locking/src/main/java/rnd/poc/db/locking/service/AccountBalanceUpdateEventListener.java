package rnd.poc.db.locking.service;

import rnd.poc.db.locking.model.Amount;
import rnd.poc.db.locking.model.BankAccount;
import rnd.poc.db.locking.model.BankAccountRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@AllArgsConstructor
public class AccountBalanceUpdateEventListener {

    private BankAccountRepository bankAccountRepository;

    private EntityManager entityManager;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCustom(AccountBalanceUpdateEvent event) {
        entityManager.flush();
        entityManager.clear();

        List<BankAccount> bankAccounts = bankAccountRepository
                .findOrderedAccountsByIdInForUpdate(event.getBankAccountsBalancesMap().keySet());

        bankAccounts.forEach(account -> {
            Amount newAmount = event.getBankAccountsBalancesMap().get(account.getId());
            account.getBalance().addAmount(newAmount);
        });
    }
}
