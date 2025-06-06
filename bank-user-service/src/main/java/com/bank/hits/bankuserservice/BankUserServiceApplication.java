package com.bank.hits.bankuserservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaRepositories
@EnableScheduling
@SpringBootApplication
public class BankUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankUserServiceApplication.class, args);
    }

}
