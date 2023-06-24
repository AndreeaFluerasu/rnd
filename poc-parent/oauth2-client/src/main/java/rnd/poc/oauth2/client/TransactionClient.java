package rnd.poc.oauth2.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import static rnd.poc.oauth2.client.OAuth2WebClient.TRX_OAUTH2_WEB_CLIENT;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Component
public class TransactionClient {

    private static final String TRANSACTION_STATUS_PATH = "/api/transactions/{0}";

    private final WebClient webClient;
    private final OAuth2ResourceServerProperties oauth2ResourceServerProperties;

    public TransactionClient(@Qualifier(TRX_OAUTH2_WEB_CLIENT) WebClient webClient,
                             OAuth2ResourceServerProperties oauth2ResourceServerProperties) {
        this.webClient = webClient;
        this.oauth2ResourceServerProperties = oauth2ResourceServerProperties;
    }

    public ResponseEntity<TransactionDTO> getTransactionStatus(String endToEndId) {
        var transactionStatusURL = UriComponentsBuilder.fromUri(oauth2ResourceServerProperties.getServerURL())
                .path(TRANSACTION_STATUS_PATH)
                .build(endToEndId).toString();

        return webClient.method(HttpMethod.GET)
                .uri(transactionStatusURL)
                .attributes(clientRegistrationId("transaction-client"))
                .retrieve()
                .toEntity(TransactionDTO.class)
                .block();
    }

    public record TransactionDTO(TransactionStatus status) {
        public enum TransactionStatus {
            FAILED,
            SUCCESSFUL,
            OPEN
        }
    }


}
