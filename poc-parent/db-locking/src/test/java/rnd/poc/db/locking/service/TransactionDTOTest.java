package rnd.poc.db.locking.service;

import rnd.poc.db.locking.model.Transaction;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionDTOTest {

    @Test
    void assertDebitCreditIndicatorEntityEnumSameAsDebitCreditIndicatorDTOEnum() {
        List<TransactionDTO.DebitCreditIndicatorDTO> debitCreditIndicatorDTOS = Arrays.stream(Transaction.DebitCreditIndicator.values())
                .map(val -> TransactionDTO.DebitCreditIndicatorDTO.valueOf(val.name()))
                .toList();

        Assertions.assertThat(debitCreditIndicatorDTOS).containsExactlyInAnyOrder(TransactionDTO.DebitCreditIndicatorDTO.values());
    }
}