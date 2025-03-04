package info.touret.bookstore.spring.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    /**
     * Defines ABAC based security policy <br/>
     * CSRF and CORS are disabled for testing purpose only ;-)
     *
     * @param http the HTTP Security configuration
     * @return
     */
    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
        /* Defaut configuration for OAUTH authorization (TO BE ADDED during the workshop)
        http.csrf().disable().cors().disable()
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/book/**").hasAnyAuthority("SCOPE_book:read", "SCOPE_book:write")
                        .pathMatchers("/isbns").hasAnyAuthority("SCOPE_number:read", "SCOPE_number:write")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );
        */
        /* If the previous configuration is applied, you would remove this following line (and the other way around) */
        http.csrf().disable().cors().disable().authorizeExchange().anyExchange().permitAll();
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(OAuth2ResourceServerProperties properties) {
        return NimbusJwtDecoder.withJwkSetUri(properties.getJwt().getJwkSetUri()).build();

    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return ReactiveJwtDecoders.fromIssuerLocation("http://127.0.0.1:8009");
    }
}
