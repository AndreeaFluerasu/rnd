package rnd.poc.multi.threading.service.transaction;

import rnd.poc.multi.threading.model.CreditTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static rnd.poc.multi.threading.service.transaction.TransactionProvisioningService.LAST_TRANSACTION;

public class TransactionConsumer implements Callable<Integer> {

    private static final int DB_BATCH_SIZE = 50;

    private final BlockingQueue<CreditTransaction> queue;
    private final TransactionSaverComponent transactionSaverComponent;
    private final AtomicInteger trxCount;

    public TransactionConsumer(BlockingQueue<CreditTransaction> queue, TransactionSaverComponent transactionSaverComponent) {
        this.queue = queue;
        this.transactionSaverComponent = transactionSaverComponent;
        this.trxCount = new AtomicInteger(0);
    }

    @Override
    public Integer call() {
        List<CreditTransaction> transactionsToBeSaved = new ArrayList<>();

        while(true) {
            CreditTransaction transactionFromQueue = takeTransactionFromQueue();

            if (transactionFromQueue == LAST_TRANSACTION) {
                transactionSaverComponent.executeInTransaction(transactionsToBeSaved);

                return trxCount.get();
            }

            transactionsToBeSaved.add(transactionFromQueue);

            if (transactionsToBeSaved.size() == DB_BATCH_SIZE) {
                transactionSaverComponent.executeInTransaction(transactionsToBeSaved);
                transactionsToBeSaved.clear();
            }
        }
    }

    private CreditTransaction takeTransactionFromQueue() {
        try {
            CreditTransaction creditTransaction = queue.take();
            trxCount.incrementAndGet();

            return creditTransaction;

        } catch (InterruptedException e) {
            throw new TransactionProcessingException("Error while taking a transaction from queue:", e);
        }
    }
}
