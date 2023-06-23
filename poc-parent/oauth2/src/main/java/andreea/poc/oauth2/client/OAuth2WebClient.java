package andreea.poc.oauth2.client;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class OAuth2WebClient {

    private static final int TIMEOUT_MILLIS = 10_000;
    private static final Duration MAX_IDLE_TIME = Duration.ofSeconds(60);

    public static final String TRX_OAUTH2_WEB_CLIENT = "TRX_OAUTH2_WEB_CLIENT";

    @Bean(name = TRX_OAUTH2_WEB_CLIENT)
    WebClient webClient(OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
                        ClientRegistrationRepository clientRegistrationRepository) {

        OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository =
                new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(oAuth2AuthorizedClientService);

        ServletOAuth2AuthorizedClientExchangeFilterFunction servletOAuth2AuthorizedClientExchangeFilterFunction =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository, oAuth2AuthorizedClientRepository);

        HttpClient httpClient = buildHttpClient();

        return WebClient.builder()
                .apply(servletOAuth2AuthorizedClientExchangeFilterFunction.oauth2Configuration())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    private static HttpClient buildHttpClient() {
        var connectionProvider =  ConnectionProvider
                .builder("defaultConnectionProvider")
                .maxIdleTime(MAX_IDLE_TIME)
                .build();

        return HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT_MILLIS)
                .responseTimeout(Duration.ofMillis(TIMEOUT_MILLIS))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS))
                );
    }
}
