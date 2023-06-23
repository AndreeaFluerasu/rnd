package andreea.poc.oauth2;

import andreea.poc.oauth2.client.OAuth2ResourceServerProperties;
import andreea.poc.oauth2.client.TransactionClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static andreea.poc.oauth2.client.OAuth2WebClient.TRX_OAUTH2_WEB_CLIENT;
import static andreea.poc.oauth2.client.TransactionClient.TransactionDTO.TransactionStatus.FAILED;
import static andreea.poc.oauth2.client.TransactionClient.TransactionDTO.TransactionStatus.SUCCESSFUL;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ApplicationMain.class)
@ActiveProfiles("it")
public class TransactionClientIT {

    private static final String OAUTH2_TOKEN_PATH = "/oauth2/token";
    private static final String TRANSACTION_PATH = "/api/transactions/%s";

    private static MockWebServer mockWebServer;

    @Autowired
    @Qualifier(TRX_OAUTH2_WEB_CLIENT)
    private WebClient webClient;

    private TransactionClient transactionClient;

    @BeforeAll
    static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8080);
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        OAuth2ResourceServerProperties oAuth2ResourceServerProperties = new OAuth2ResourceServerProperties();
        oAuth2ResourceServerProperties.setServerURL(mockWebServer.url("").uri());

        this.transactionClient = new TransactionClient(webClient, oAuth2ResourceServerProperties);
    }

    @Test
    void retrievingStatusesForMultipleTransactions() throws InterruptedException {
        var accessToken = "ACxsxdEGHbx8VJ80";
        mockAccessTokenResponse(accessToken);

        mockTransactionStatusResponse("SUCCESSFUL");
        mockTransactionStatusResponse("FAILED");

        var successfulTrxEndToEndId = "d31dd76981ca41f6a29f701fb43dd4a6";
        var failedTrxEndToEndId = "a31dd76981ca41f6a29f701fb43dd4a2";

        TransactionClient.TransactionDTO firstTransactionResponse = this.transactionClient.getTransactionStatus(successfulTrxEndToEndId).getBody();
        TransactionClient.TransactionDTO secondTransactionResponse = this.transactionClient.getTransactionStatus(failedTrxEndToEndId).getBody();

        // assert access token request
        RecordedRequest tokenRequest = mockWebServer.takeRequest();
        assertEquals(OAUTH2_TOKEN_PATH, tokenRequest.getPath());
        assertEquals("Basic b2F1dGgyLWNsaWVudC1zZXJ2ZXI6b2F1dGgyLWNsaWVudC1zZXJ2ZXItc2VjcmV0", tokenRequest.getHeader(AUTHORIZATION)); // base64(clientId:clientSecret)

        // assert first transaction response
        RecordedRequest successfulTrxRequest = mockWebServer.takeRequest();
        assertEquals(TRANSACTION_PATH.formatted(successfulTrxEndToEndId), successfulTrxRequest.getPath());
        assertEquals("Bearer " + accessToken, successfulTrxRequest.getHeader(AUTHORIZATION));
        assertEquals(SUCCESSFUL, requireNonNull(firstTransactionResponse).status());

        // assert second transaction response
        RecordedRequest failedTrxRequest = mockWebServer.takeRequest();
        assertEquals(TRANSACTION_PATH.formatted(failedTrxEndToEndId), failedTrxRequest.getPath());
        assertEquals("Bearer " + accessToken, failedTrxRequest.getHeader(AUTHORIZATION));
        assertEquals(FAILED, requireNonNull(secondTransactionResponse).status());
    }

    private void mockAccessTokenResponse(String accessToken) {
        mockWebServer.enqueue(
            new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(
                    """
                        {
                            "access_token": "%s",
                            "token_type": "Bearer",
                            "expires_in": 300
                        }
                    """.formatted(accessToken)
                )
        );
    }

    private void mockTransactionStatusResponse(String status) {
        mockWebServer.enqueue(
            new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(
                    """
                        {
                            "status": "%s"
                        }
                     """.formatted(status)
                )
        );
    }
}
