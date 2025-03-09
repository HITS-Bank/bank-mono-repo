package com.bank.hits.bankcreditservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class BankCreditServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankCreditServiceApplication.class, args);
    }

}
