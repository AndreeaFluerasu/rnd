package rnd.poc.multi.threading.service.transaction;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import rnd.poc.multi.threading.model.CreditTransaction;
import rnd.poc.multi.threading.service.ExecutionTimeLoggable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@AllArgsConstructor
public class TransactionProvisioningService {

    public static final CreditTransaction LAST_TRANSACTION = CreditTransaction.builder().build();

    public static final int CONSUMER_THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 2;
    public static final int PRODUCER_THREAD_COUNT = 1;

    private static final int CONSUMER_QUEUE_SIZE = 100;

    private TransactionSaverComponent transactionSaverComponent;

    @ExecutionTimeLoggable
    public List<Integer> execute() {
        try(ExecutorService executorService = Executors.newFixedThreadPool(PRODUCER_THREAD_COUNT + CONSUMER_THREAD_COUNT)) {

            ExecutorCompletionService<Integer> executorCompletionService = new ExecutorCompletionService<>(executorService);

            List<BlockingQueue<CreditTransaction>> queues = new ArrayList<>();

            for (int i=0; i<CONSUMER_THREAD_COUNT; i++) {
                TransactionConsumer transactionConsumer = buildTransactionConsumer(queues);
                executorCompletionService.submit(transactionConsumer);
            }

            TransactionProducer transactionProducer = new TransactionProducer(queues);
            executorCompletionService.submit(transactionProducer);

            return getProvisioningResult(executorCompletionService);
        }
    }

    private List<Integer> getProvisioningResult(ExecutorCompletionService<Integer> executorCompletionService) {
        try {
            List<Integer> result = new ArrayList<>();

            for (int i = 0; i < PRODUCER_THREAD_COUNT + CONSUMER_THREAD_COUNT; i++) {
                Integer provisionedTrxCount = executorCompletionService.take().get();
                result.add(provisionedTrxCount);
            }

            return result;

        } catch (InterruptedException |ExecutionException e) {
            throw new TransactionProcessingException("Error while provisioning transactions:", e);
        }
    }

    private TransactionConsumer buildTransactionConsumer(List<BlockingQueue<CreditTransaction>> queues) {
        BlockingQueue<CreditTransaction> queue = new ArrayBlockingQueue<>(CONSUMER_QUEUE_SIZE);
        queues.add(queue);

        return new TransactionConsumer(queue, transactionSaverComponent);
    }

}
