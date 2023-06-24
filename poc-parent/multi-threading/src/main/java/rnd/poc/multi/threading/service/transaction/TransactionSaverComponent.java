package rnd.poc.multi.threading.service.transaction;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rnd.poc.multi.threading.model.CreditTransaction;
import rnd.poc.multi.threading.model.CreditTransactionRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class TransactionSaverComponent {

    private CreditTransactionRepository creditTransactionRepository;

    @Transactional
    public void executeInTransaction(List<CreditTransaction> creditTransactions) {
        creditTransactionRepository.saveAll(creditTransactions);
    }
}
