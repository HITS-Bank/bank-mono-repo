package com.bank.hits.bankcoreservice.config.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.ciklon.bank.bankcoreservice.api.dto.AccountDto;
import ru.ciklon.bank.bankcoreservice.api.dto.ClientInfoDto;
import ru.ciklon.bank.bankcoreservice.api.dto.CreditApprovedDto;
import ru.ciklon.bank.bankcoreservice.api.dto.CreditPaymentResponseDTO;
import ru.ciklon.bank.bankcoreservice.api.dto.CreditRepaymentRequest;
import ru.ciklon.bank.bankcoreservice.api.dto.OpenAccountDto;
import ru.ciklon.bank.bankcoreservice.core.service.AccountService;
import ru.ciklon.bank.bankcoreservice.core.service.ClientService;
import ru.ciklon.bank.bankcoreservice.core.service.CreditService;
import ru.ciklon.bank.bankcoreservice.core.service.EmployeeService;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountEventConsumer {

    private final AccountService accountService;
    private final CreditService creditService;
    private final EmployeeService employeeService;
    private final KafkaProducerService kafkaProducerService;
    private final ClientService clientService;

    @KafkaListener(topics = "create.account", groupId = "bank.group")
    public void handleCreateAccount(final ConsumerRecord<String, OpenAccountDto> record) {
        log.info("Received create.account event: {}", record.value());
        try {
            final AccountDto createdAccount = accountService.openAccount(record.value());
            log.info("Account created successfully: {}", createdAccount);
        } catch (Exception e) {
            log.error("Error processing create.account event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "close.account", groupId = "bank.group")
    public void handleCloseAccount(final ConsumerRecord<String, UUID> record) {
        log.info("Received close.account event: {}", record.value());
        try {
            accountService.closeAccount(record.value());
            log.info("Account closed successfully: {}", record.value());
        } catch (Exception e) {
            log.error("Error processing close.account event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "credit.approved", groupId = "bank.group")
    public void handleCreditApproved(final ConsumerRecord<String, CreditApprovedDto> record) {
        log.info("Received credit.create event: {}", record.value());
        try {
            creditService.processCreditApproval(record.value());
            log.info("Credit created successfully for client {}", record.value().getClientId());
        } catch (Exception e) {
            log.error("Error processing credit.create event: {}", e.getMessage(), e);
        }
    }


    @KafkaListener(topics = "block.account", groupId = "bank.group")
    public void handleBlockAccount(final ConsumerRecord<String, UUID> record) {
        log.info("Received block.account event: {}", record.value());
        try {
            final UUID clientId = record.value();
            accountService.blockAccount(clientId);
            log.info("Accounts blocked for client {}", clientId);
        } catch (Exception e) {
            log.error("Error processing block.account event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "unblock.account", groupId = "bank.group")
    public void handleUnblockAccount(final ConsumerRecord<String, UUID> record) {
        log.info("Received unblock.account event: {}", record.value());
        try {
            final UUID clientId = record.value();
            accountService.unblockAccount(clientId);
            log.info("Accounts unblocked for client {}", clientId);
        } catch (Exception e) {
            log.error("Error processing unblock.account event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "block.employee", groupId = "bank.group")
    public void handleBlockEmployee(final ConsumerRecord<String, UUID> record) {
        log.info("Received block.employee event: {}", record.value());
        try {
            final UUID employeeId = record.value();
            employeeService.blockEmployee(employeeId);
            log.info("Employee blocked {}", employeeId);
        } catch (Exception e) {
            log.error("Error processing block.employee event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "unblock.employee.request", groupId = "bank.group")
    public void handleUnblockEmployee(final ConsumerRecord<String, UUID> record) {
        log.info("Received unblock.employee event: {}", record.value());
        try {
            final UUID correlationId = parseCorrelationId(record);
            if (correlationId == null) { return;}

            final UUID employeeId = record.value();
            employeeService.unblockEmployee(employeeId);
            log.info("Employee unblocked {}", employeeId);
        } catch (Exception e) {
            log.error("Error processing unblock.employee event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "credit.client.info.request", groupId = "bank.group")
    public void handleClientInfoRequest(final ConsumerRecord<String, String> record) {
        log.info("Received client info request: {}", record.value());
        try {
            final UUID correlationId = parseCorrelationId(record);
            if (correlationId == null) { return;}

            final UUID clientId = UUID.fromString(record.value());

            final ClientInfoDto clientInfo = clientService.getClientInfoForCredit(clientId);
            kafkaProducerService.sendUserInfoForCredit(clientInfo,correlationId);
            log.info("Client info sent for client {}", clientId);
        } catch (Exception e) {
            log.error("Error processing client info request: {}", e.getMessage(), e);
        }
    }


    @KafkaListener(topics = "credit.payment.request", groupId = "bank.group")
    public void handleCreditRepayment(final ConsumerRecord<String, CreditRepaymentRequest> record) {
        try {
            final UUID correlationId = parseCorrelationId(record);
            if (correlationId == null) { return;}
            final CreditPaymentResponseDTO response = accountService.repayCredit(record.value());
            kafkaProducerService.sendCreditPaymentResponse(response, correlationId);
            log.info("Credit repayment processed for application {}", record.value().getCreditContractId());
        } catch (Exception e) {
            log.error("Error processing credit repayment event: {}", e.getMessage(), e);
        }
    }

    private UUID parseCorrelationId(final ConsumerRecord<String, ?> record) {
        final Header header = record.headers().lastHeader("correlation_id");
        if (header == null) {
            log.warn("Получено сообщение без заголовка correlation_id");
            return null;
        }
        final UUID correlationId = UUID.fromString(new String(header.value()));

        return correlationId;
    }
}
