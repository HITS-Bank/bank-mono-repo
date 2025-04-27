package com.bank.hits.banknotificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@SpringBootApplication
public class BankNotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankNotificationServiceApplication.class, args);
    }
}
