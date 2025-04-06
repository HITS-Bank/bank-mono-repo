package com.bank.hits.bankpersonalizationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@SpringBootApplication
public class BankPersonalizationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankPersonalizationServiceApplication.class, args);
    }

}
