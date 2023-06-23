package andreea.poc.oauth2.controller;

import andreea.poc.oauth2.client.TransactionClient;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TransactionController {

    private TransactionClient transactionClient;

    @GetMapping("/api/transactions/{endToEndId}")
    public TransactionClient.TransactionDTO getStatus(@PathVariable String endToEndId) {
        return transactionClient
                .getTransactionStatus(endToEndId)
                .getBody();
    }
}
