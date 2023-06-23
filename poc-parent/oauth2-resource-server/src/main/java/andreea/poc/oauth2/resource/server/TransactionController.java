package andreea.poc.oauth2.resource.server;

import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
public class TransactionController {

    private static final Map<String, String> DUMMY_TRANSACTIONS = Map.ofEntries(
            Map.entry("e2eId_1", "SUCCESSFUL"),
            Map.entry("e2eId_2", "FAILED"),
            Map.entry("e2eId_3", "OPEN")
            );

    @GetMapping("/api/transactions/{endToEndId}")
    @PreAuthorize("hasAuthority('SCOPE_transaction-status:read')")
    public TransactionStatusAPI getTransactionStatus(@PathVariable String endToEndId) {
        if (!DUMMY_TRANSACTIONS.containsKey(endToEndId)) {
            throw new IllegalArgumentException("Transaction with endToEndId=%s not found".formatted(endToEndId));
        }

        return new TransactionStatusAPI(endToEndId, DUMMY_TRANSACTIONS.get(endToEndId));
    }

    public record TransactionStatusAPI(String endToEndId, String status) {

    }
}
