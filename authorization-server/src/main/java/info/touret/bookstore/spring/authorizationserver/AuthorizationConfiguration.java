package info.touret.bookstore.spring.authorizationserver;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;


/**
 * Defines the security policy applied in this workshop.
 * It is based on <a href="https://www.oauth.com/oauth2-servers/access-tokens/client-credentials/">OAUTH2 Credential Flow</a>
 * to make the workshop easy to be tested.
 */
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class AuthorizationConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationConfiguration.class);
    private final AuthorizationClientsProperties authorizationClientsProperties;

    public AuthorizationConfiguration(AuthorizationClientsProperties authorizationClientsProperties) {
        this.authorizationClientsProperties = authorizationClientsProperties;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        return http.formLogin(Customizer.withDefaults()).build();
    }

    /**
     * Stores all the clients defined in the application.properties file under the <pre>authorization.clients</pre> prefix.
     * For instance: <pre>authorization.clients.customer1.clientId=customer1</pre>
     *
     * @return The registry of all the clients
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        var clientRepositories = authorizationClientsProperties.getClients().entrySet().stream().map(
                client -> {
                    LOGGER.info("Creating [{},{},{}] client repository", client.getKey(), client.getValue().getClientSecret(), client.getValue().getScopes());
                    return RegisteredClient.withId(UUID.randomUUID().toString())
                            .clientId(client.getValue().getClientId())
                            .clientSecret("{noop}" + client.getValue().getClientSecret())
                            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                            .scopes(scopes -> scopes.addAll(client.getValue().getScopes()))
                            .build();
                }).toList();
        LOGGER.info("[{}] client repositories created", clientRepositories.size());
        return new InMemoryRegisteredClientRepository(clientRepositories);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    private static RSAKey generateRsa() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }
}
