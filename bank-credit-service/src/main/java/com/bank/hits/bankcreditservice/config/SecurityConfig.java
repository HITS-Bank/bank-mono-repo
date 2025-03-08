package com.bank.hits.bankcreditservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // Разрешаем все запросы
                .csrf(csrf -> csrf.disable()) // Отключаем CSRF
                .build();
    }
}


/*

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                //.csrf(AbstractHttpConfigurer::disable) // Отключаем CSRF для API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/api/tariffs/**", "POST")).authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                ))
                .build();
    }

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() { //сейчас принимаем ЛЮБОЙ токен  и не делаем верификацию тут
        return jwt -> {
            String userId = jwt.getClaimAsString("userId"); // Извлекаем userId
            if (userId == null) {
                throw new IllegalArgumentException("JWT не содержит userId");
            }

            return new JwtAuthenticationToken(jwt, Collections.emptyList(), userId);
        };
    }
}

 */
