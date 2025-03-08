package com.bank.hits.bankcoreservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@SpringBootApplication
public class BankCoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankCoreServiceApplication.class, args);
    }

}
