package com.bank.hits.bankgatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BankGatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankGatewayServiceApplication.class, args);
    }

    @Bean
    public UserIdFilter userIdFilter() {
        return new UserIdFilter();
    }
}
