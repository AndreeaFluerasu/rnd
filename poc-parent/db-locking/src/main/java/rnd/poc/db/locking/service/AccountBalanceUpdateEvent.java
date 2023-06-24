package rnd.poc.db.locking.service;

import rnd.poc.db.locking.model.Amount;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

@Getter
public class AccountBalanceUpdateEvent extends ApplicationEvent {

    private Map<Long, Amount> bankAccountsBalancesMap;

    public AccountBalanceUpdateEvent(Object source, Map<Long, Amount> bankAccountsBalancesMap) {
        super(source);
        this.bankAccountsBalancesMap = bankAccountsBalancesMap;
    }
}
