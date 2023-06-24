package rnd.poc.multi.threading.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import rnd.poc.multi.threading.service.transaction.TransactionProvisioningService;

import java.util.List;

@RestController
@AllArgsConstructor
public class TransactionProvisioningController {

    private TransactionProvisioningService transactionProvisioningService;

    @PostMapping("/credit-transactions:generate")
    public List<Integer> provisionTransactions() {
        return transactionProvisioningService.execute();
    }
}
