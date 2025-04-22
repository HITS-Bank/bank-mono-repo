package com.bank.hits.bankcoreservice.config.service;

import com.bank.hits.bankcoreservice.api.dto.CloseAccountRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.bank.hits.bankcoreservice.api.dto.AccountDto;
import com.bank.hits.bankcoreservice.api.dto.ClientInfoDto;
import com.bank.hits.bankcoreservice.api.dto.CreditApprovedDto;
import com.bank.hits.bankcoreservice.api.dto.CreditPaymentResponseDTO;
import com.bank.hits.bankcoreservice.api.dto.CreditRepaymentRequest;
import com.bank.hits.bankcoreservice.api.dto.OpenAccountDto;
import com.bank.hits.bankcoreservice.core.service.AccountService;
import com.bank.hits.bankcoreservice.core.service.ClientService;
import com.bank.hits.bankcoreservice.core.service.CreditService;
import com.bank.hits.bankcoreservice.core.service.EmployeeService;

import java.util.UUID;

import static com.bank.hits.bankcoreservice.core.utils.ExceptionUtils.throwExceptionRandomly;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountEventConsumer {

    private final AccountService accountService;
    private final CreditService creditService;
    private final EmployeeService employeeService;
    private final KafkaProducerService kafkaProducerService;
    private final ClientService clientService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "create.account", groupId = "bank.group")
    public void handleCreateAccount(final ConsumerRecord<String, UUID> record) {
        /*
        log.info("Received create.account event: {}", record.value());
        try {
            final AccountDto createdAccount = accountService.openAccount(record.value());
            log.info("Account created successfully: {}", createdAccount);
        } catch (Exception e) {
            log.error("Error processing create.account event: {}", e.getMessage(), e);
        }

         */
    }

    @KafkaListener(topics = "close.account", groupId = "bank.group")
    public void handleCloseAccount(final ConsumerRecord<String, CloseAccountRequest> record) {
        log.info("Received close.account event: {}", record.value());
        try {
            accountService.closeAccount(record.value());
            log.info("Account closed successfully: {}", record.value());
        } catch (Exception e) {
            log.error("Error processing close.account event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "credit.approved.request", groupId = "bank.group")
    public void handleCreditApproved(final ConsumerRecord<String, String> record) {
        log.info("Received credit.create event: {}", record.value());
        try {
            throwExceptionRandomly();
            final UUID correlationId = parseCorrelationId(record);
            if(correlationId == null)
            {
                throw  new RuntimeException("Получено сообщение без correlationId");
            }
            CreditApprovedDto creditApprovedDto = objectMapper.readValue(record.value(), CreditApprovedDto.class);
            log.info("creditApproveDTO: {}", creditApprovedDto);
            creditService.processCreditApproval(creditApprovedDto,correlationId);
            log.info("Credit created successfully for client {}", creditApprovedDto.getClientId());
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
            throwExceptionRandomly();
            final UUID correlationId = parseCorrelationId(record);
            if (correlationId == null) { return;}

            final UUID clientId = UUID.fromString(record.value());
            log.info("Client info request for client {}", clientId);

            final ClientInfoDto clientInfo = clientService.getClientInfoForCredit(clientId);
            log.info("clientInfo = {}", clientInfo);
            kafkaProducerService.sendUserInfoForCredit(clientInfo,correlationId);
            log.info("Client info sent for client {}", clientId);
        } catch (Exception e) {
            log.error("Error processing client info request: {}", e.getMessage(), e);
        }
    }


    @KafkaListener(topics = "credit.payment.request", groupId = "bank.group")
    public void handleCreditRepayment(final ConsumerRecord<String, String> record) {
        log.info("record: {}", record.value());
        try {
            throwExceptionRandomly();
            CreditRepaymentRequest repaymentRequest = objectMapper.readValue(record.value(), CreditRepaymentRequest.class);
            final UUID correlationId = parseCorrelationId(record);
            if (correlationId == null) { return;}
            log.info("creditContractId пришел: {}", repaymentRequest.getCreditContractId());
            final CreditPaymentResponseDTO response = accountService.repayCredit(repaymentRequest);
            kafkaProducerService.sendCreditPaymentResponse(response, correlationId);
            log.info("Credit repayment processed for application {}", repaymentRequest.getCreditContractId());
        } catch (Exception e) {
            log.error("Error processing credit repayment event: {}", e.getMessage(), e);
        }
    }

    private UUID parseCorrelationId(final ConsumerRecord<String, ?> record) {
        final Header header = record.headers().lastHeader("correlation_id");
        log.info("header: {}", header);
        log.info("headers: {}", record.headers());
        log.info("correlationId: {}", header.value());
        log.info("record.value: {}", record.value());
        if (header == null) {
            log.warn("Получено сообщение без заголовка correlation_id");
            return null;
        }
        log.info("correlation_id: {}", new String(header.value()));
        final UUID correlationId = UUID.fromString(new String(header.value()));

        return correlationId;
    }
}
