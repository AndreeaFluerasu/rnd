package rnd.poc.db.locking.service;

import rnd.poc.db.locking.DBLockingAppMain;
import rnd.poc.db.locking.model.BankAccount;
import rnd.poc.db.locking.model.BankAccountRepository;
import rnd.poc.db.locking.model.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Sql(scripts = "/reset.sql")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DBLockingAppMain.class)
@ActiveProfiles("it")
public class InboundTransactionProcessingServiceIT {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private InboundTransactionProcessingService inboundTransactionProcessingService;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    @DisplayName("Processing multiple transactions concurrently should update account balances correctly")
    void concurrentlyProcessingTransactions() throws ExecutionException, InterruptedException {
        BankAccount bankAccount_1 = BankAccountFixture.aBankAccount().iban("BE24363176811438").eurBalance(valueOf(100)).build();
        BankAccount bankAccount_2 = BankAccountFixture.aBankAccount().iban("NL89RABO0316724696").eurBalance(valueOf(0)).build();

        bankAccountRepository.save(bankAccount_1);
        bankAccountRepository.save(bankAccount_2);

        processTasksInParallel(
                () -> {
                    List<TransactionDTO> transactionsTask_1 = List.of(
                            TransactionDTOFixture.aTransactionDTO().credit().eurAmount(valueOf(10)).account(bankAccount_1).build(),
                            TransactionDTOFixture.aTransactionDTO().debit().eurAmount(valueOf(20)).account(bankAccount_2).build(),
                            TransactionDTOFixture.aTransactionDTO().debit().eurAmount(valueOf(30)).account(bankAccount_1).build()
                    );

                    inboundTransactionProcessingService.processTransactionBatch(transactionsTask_1);
                    return true;
                },

                () -> {
                    List<TransactionDTO> transactionsTask_2 = List.of(
                            TransactionDTOFixture.aTransactionDTO().debit().eurAmount(valueOf(15)).account(bankAccount_1).build(),
                            TransactionDTOFixture.aTransactionDTO().credit().eurAmount(valueOf(25)).account(bankAccount_2).build(),
                            TransactionDTOFixture.aTransactionDTO().credit().eurAmount(valueOf(35)).account(bankAccount_2).build()
                    );

                    inboundTransactionProcessingService.processTransactionBatch(transactionsTask_2);
                    return true;
                }
        );

        assertThat(transactionRepository.count()).isEqualTo(6);

        assertThat(bankAccountRepository.findByIban(bankAccount_1.getIban()))
                .isPresent()
                .hasValueSatisfying(account -> assertThat(account.getBalance().getValue()).isEqualByComparingTo(valueOf(65)));

        assertThat(bankAccountRepository.findByIban(bankAccount_2.getIban()))
                .isPresent()
                .hasValueSatisfying(account -> assertThat(account.getBalance().getValue()).isEqualByComparingTo(valueOf(40)));

    }

    private void processTasksInParallel(Callable<Boolean> task_1, Callable<Boolean> task_2) throws ExecutionException, InterruptedException {
        try(ExecutorService executorService = Executors.newFixedThreadPool(2)) {

            Future<Boolean> taskFuture_1 = executorService.submit(task_1);
            Future<Boolean> taskFuture_2 = executorService.submit(task_2);

            executorService.shutdown();
            executorService.awaitTermination(10000, TimeUnit.SECONDS);

            assertTrue(taskFuture_1.get());
            assertTrue(taskFuture_2.get());
        }
    }
}
