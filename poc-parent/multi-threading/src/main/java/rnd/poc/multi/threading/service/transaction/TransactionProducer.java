package rnd.poc.multi.threading.service.transaction;

import rnd.poc.multi.threading.model.CreditTransaction;
import rnd.poc.multi.threading.service.RandomValueGenerator;

import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static rnd.poc.multi.threading.service.transaction.TransactionProvisioningService.LAST_TRANSACTION;

public class TransactionProducer implements Callable<Integer> {

    private static final int BIT_SHIFT_DISTRIBUTION_VALUE = 0x7FFFFFFF;

    private final List<BlockingQueue<CreditTransaction>> queues;
    private final AtomicInteger trxCount;

    public TransactionProducer(List<BlockingQueue<CreditTransaction>> queues) {
        this.queues = queues;
        this.trxCount = new AtomicInteger(0);
    }

    @Override
    public Integer call() {
        for (int i=0; i < 100000; i++) {
            BlockingQueue<CreditTransaction> queue = getRandomQueue();

            CreditTransaction creditTransaction = buildTransaction();

            addTransactionToQueue(creditTransaction, queue);
        }
        addLastTransactionToQueues();

        return trxCount.get();

    }

    private BlockingQueue<CreditTransaction> getRandomQueue() {
        return queues.get((UUID.randomUUID().hashCode() & BIT_SHIFT_DISTRIBUTION_VALUE) % queues.size());
    }

    private void addTransactionToQueue(CreditTransaction creditTransaction, BlockingQueue<CreditTransaction> queue) {
        try {
            queue.put(creditTransaction);
            trxCount.incrementAndGet();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void addLastTransactionToQueues() {
        try {
            for (BlockingQueue<CreditTransaction> queue : queues) {
                queue.put(LAST_TRANSACTION);
                trxCount.incrementAndGet();
            }
        } catch (InterruptedException e) {
            throw new TransactionProcessingException("Error while adding the last transactions to all queues:", e);
        }
    }

    private CreditTransaction buildTransaction() {
        return CreditTransaction.builder()
                .amountValue(RandomValueGenerator.randomBigDecimal())
                .amountCurrency(Currency.getInstance("EUR"))
                .payerName(RandomValueGenerator.randomString(20))
                .endToEndId(UUID.randomUUID().toString().replace("-", ""))
                .reference(UUID.randomUUID().toString())
                .beneficiaryPublicIdentifier(UUID.randomUUID())
                .build();
    }
}
