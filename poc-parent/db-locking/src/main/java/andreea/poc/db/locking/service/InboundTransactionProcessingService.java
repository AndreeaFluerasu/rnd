package andreea.poc.db.locking.service;

import andreea.poc.db.locking.model.Amount;
import andreea.poc.db.locking.model.Transaction;
import andreea.poc.db.locking.model.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class InboundTransactionProcessingService {

    private TransactionDTOMapper transactionDTOMapper;

    private TransactionRepository transactionRepository;

    private ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void processTransactionBatch(List<TransactionDTO> transactionDTOs) {
        Map<Long, Amount> accountBalanceMap = new HashMap<>();

        transactionDTOs.forEach(transactionDTO -> {
                var transaction = createTransaction(transactionDTO);

                updateAccountBalanceMap(transaction, accountBalanceMap);
            }
        );

        applicationEventPublisher.publishEvent(new AccountBalanceUpdateEvent(this, accountBalanceMap));
    }

    private void updateAccountBalanceMap(Transaction transaction, Map<Long, Amount> accountBalanceMap) {
        accountBalanceMap.compute(
                transaction.getBankAccount().getId(),
                (bankAccountId, balance) -> balance == null ? transaction.getSignedAmount() : Amount.sum(balance, transaction.getSignedAmount())
        );
    }

    private Transaction createTransaction(TransactionDTO transactionDTO) {
        Transaction transaction = transactionDTOMapper.toEntity(transactionDTO);

        return transactionRepository.save(transaction);
    }
}
