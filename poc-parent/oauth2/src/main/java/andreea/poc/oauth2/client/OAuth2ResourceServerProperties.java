package andreea.poc.oauth2.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Getter
@Setter
@Configuration
@ConfigurationProperties("oauth2.resource")
public class OAuth2ResourceServerProperties {

    private URI serverURL;
}
