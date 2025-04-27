package com.bank.hits.bankcoreservice.config.service;

import com.bank.hits.bankcoreservice.api.dto.AccountOperationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import com.bank.hits.bankcoreservice.api.dto.ClientInfoDto;
import com.bank.hits.bankcoreservice.api.dto.CreditAccountCreatedResponse;
import com.bank.hits.bankcoreservice.api.dto.CreditPaymentResponseDTO;
import com.bank.hits.bankcoreservice.core.entity.Account;
import com.bank.hits.bankcoreservice.core.entity.CreditContract;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(final String topic, final String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
        sendSync(record);
    }

    @CircuitBreaker(name = "kafkaProducerService", fallbackMethod = "sendUserInfoForCreditFallback")
    @Retry(name = "kafkaProducerService")
    public void sendUserInfoForCredit(final ClientInfoDto clientInfoDto, UUID correlationId) {
        try {
            final String message = objectMapper.writeValueAsString(clientInfoDto);
            log.info("message = {}", message);
            ProducerRecord<String, String> record = new ProducerRecord<>("credit.client.info.response", message);
            record.headers().add("event_type", "get_credit_client_info".getBytes());
            record.headers().add("correlation_id",(String.valueOf(correlationId).getBytes()));
            record.headers().add("timeoutExpire", "30".getBytes());
            sendSync(record);
            log.info("Sent USER_INFO_FOR_CREDIT event: {}", message);
        } catch (final Exception e) {
            log.error("Error sending USER_INFO_FOR_CREDIT event", e);
            throw new RuntimeException("Error sending USER_INFO_FOR_CREDIT", e);
        }
    }

    private void sendUserInfoForCreditFallback(ClientInfoDto dto, UUID correlationId, Throwable t) {
        log.error("FALLBACK USER_INFO_FOR_CREDIT for correlationId {}: {}", correlationId, t.toString());
    }

    @CircuitBreaker(name = "kafkaProducerService", fallbackMethod = "sendCreditApprovedFallback")
    @Retry(name = "kafkaProducerService")
    public void sendCreditApproved(final boolean approved, UUID correlationId) {
        try {
            final String message = objectMapper.writeValueAsString(approved);
            log.info("message = {}", message);
            ProducerRecord<String, String> record = new ProducerRecord<>("credit.approved.response", message);
            record.headers().add("correlation_id",(String.valueOf(correlationId).getBytes()));
            record.headers().add("timeoutExpire", "30".getBytes());
            sendSync(record);
            log.info("Sent CREDIT_AAPROVED event: {}", message);
        } catch (final Exception e) {
            log.error("Error sending CREDIT_APRROVED event", e);
            throw new RuntimeException("Error sending CREDIT_APPROVED", e);
        }
    }

    private void sendCreditApprovedFallback(boolean approved, UUID correlationId, Throwable t) {
        log.error("FALLBACK CREDIT_APPROVED for correlationId {}: {}", correlationId, t.toString());
    }

    @CircuitBreaker(name = "kafkaProducerService", fallbackMethod = "sendCreditAccountCreatedFallback")
    @Retry(name = "kafkaProducerService")
    public void sendCreditAccountCreatedEvent(final CreditContract creditContract, final Account creditAccount) {
        try {
            final CreditAccountCreatedResponse event = new CreditAccountCreatedResponse(
                    creditContract.getCreditContractId(),
                    creditAccount.getId(),
                    String.valueOf(creditContract.getCreditAmount())
            );
            String payload = objectMapper.writeValueAsString(event);
            ProducerRecord<String, String> record =
                    new ProducerRecord<>("credit.account.created.response", payload);

            sendSync(record);
            log.info("Sent CREDIT_ACCOUNT_CREATED event: {}", event);
        } catch (final Exception e) {
            log.error("Error sending CREDIT_ACCOUNT_CREATED event", e);
            throw new RuntimeException("Error sending CREDIT_ACCOUNT_CREATED", e);
        }
    }

    private void sendCreditAccountCreatedFallback(CreditContract cc, Account acc, Throwable t) {
        log.error("FALLBACK CREDIT_ACCOUNT_CREATED for contract {}: {}", cc.getCreditContractId(), t.toString());
    }

    @CircuitBreaker(name = "kafkaProducerService", fallbackMethod = "sendCreditPaymentResponseFallback")
    @Retry(name = "kafkaProducerService")

    public void sendCreditPaymentResponse(final CreditPaymentResponseDTO response, final UUID correlationId) {
        try {
            final String message = objectMapper.writeValueAsString(response);
            ProducerRecord<String, String> record = new ProducerRecord<>("credit.payment.response", message);
            record.headers().add("correlation_id",(String.valueOf(correlationId).getBytes()));
            record.headers().add("timeoutExpire", "30".getBytes());

            sendSync(record);
            log.info("Sent CREDIT_PAYMENT_RESPONSE event: {}", message);
        } catch (final Exception e) {
            log.error("Error sending CREDIT_PAYMENT_RESPONSE event", e);
            throw new RuntimeException("Error sending CREDIT_PAYMENT_RESPONSE", e);
        }

    }

    private void sendCreditPaymentResponseFallback(CreditPaymentResponseDTO resp,
                                                   UUID correlationId,
                                                   Throwable t) {
        log.error("FALLBACK CREDIT_PAYMENT_RESPONSE for correlationId {}: {}", correlationId, t.toString());
    }


    @CircuitBreaker(name = "kafkaProducerService", fallbackMethod = "sendOperationEventFallback")
    @Retry(name = "kafkaProducerService")
    public void sendOperationEvent(final AccountOperationEvent dto) {
        try {
            final String message = objectMapper.writeValueAsString(dto);
            ProducerRecord<String, String> record = new ProducerRecord<>("core.operations", message);
            record.headers().add("timeoutExpire", "30".getBytes());

            sendSync(record);
            log.info("Sent OPERATION_EVENT event: {}", message);
        } catch (final Exception e) {
            log.error("Error sending OPERATION_EVENT event", e);
            throw new RuntimeException("Error sending OPERATION_EVENT", e);
        }

    }

    private void sendOperationEventFallback(CreditPaymentResponseDTO resp,
                                                   UUID correlationId,
                                                   Throwable t) {
        log.error("FALLBACK OPERATION_EVENT_RESPONSE for correlationId {}: {}", correlationId, t.toString());
    }

    private void sendSync(ProducerRecord<String, String> record) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(record);
        try {
            future.get(5, TimeUnit.SECONDS);
            log.info("Sent to {} – payload={}", record.topic(), record.value());
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка отправки в Kafka topic=" + record.topic(), ex);
        }
    }

}
