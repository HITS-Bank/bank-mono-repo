package com.bank.hits.bankuserservice.common.config;

import com.bank.hits.bankuserservice.common.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${keycloak.mobile-app-host-uri}")
    private String mobileAppHostUri;

    @Value("${keycloak.host-uri}")
    private String hostUri;

    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("users/employee/**").hasRole(Role.EMPLOYEE.name())
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(
                        oauth2 -> oauth2
                                .authenticationManagerResolver(jwtIssuerResolver())
                )
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public JwtIssuerAuthenticationManagerResolver jwtIssuerResolver() {
        Map<String, AuthenticationManager> authManagers = new LinkedHashMap<>();
        authManagers.put(mobileAppHostUri + "/realms/bank", authenticationManager("http://keycloak:8080"));
        authManagers.put(hostUri + "/realms/bank", authenticationManager("http://keycloak:8080"));

        return new JwtIssuerAuthenticationManagerResolver(authManagers::get);
    }

    private AuthenticationManager authenticationManager(String issuerUri) {
        JwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(issuerUri + "/realms/bank/protocol/openid-connect/certs").build();
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwtDecoder);
        provider.setJwtAuthenticationConverter(jwtAuthConverter);
        return provider::authenticate;
    }
}
