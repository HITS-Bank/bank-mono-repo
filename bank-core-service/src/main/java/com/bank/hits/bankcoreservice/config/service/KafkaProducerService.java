package com.bank.hits.bankcoreservice.config.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.bank.hits.bankcoreservice.api.dto.ClientInfoDto;
import com.bank.hits.bankcoreservice.api.dto.CreditAccountCreatedResponse;
import com.bank.hits.bankcoreservice.api.dto.CreditPaymentResponseDTO;
import com.bank.hits.bankcoreservice.core.entity.Account;
import com.bank.hits.bankcoreservice.core.entity.CreditContract;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(final String topic, final String message) {
        kafkaTemplate.send(topic, message);
    }

    public void sendUserInfoForCredit(final ClientInfoDto clientInfoDto, UUID correlationId) {
        try {
            final String message = objectMapper.writeValueAsString(clientInfoDto);
            log.info("message = {}", message);
            ProducerRecord<String, String> record = new ProducerRecord<>("credit.client.info.response", message);
            record.headers().add("event_type", "get_credit_client_info".getBytes());
            record.headers().add("correlation_id",(String.valueOf(correlationId).getBytes()));
            record.headers().add("timeoutExpire", "30".getBytes());
            kafkaTemplate.send(record);
            log.info("Sent USER_INFO_FOR_CREDIT event: {}", message);
        } catch (final Exception e) {
            log.error("Error sending USER_INFO_FOR_CREDIT event", e);
        }
    }

    public void sendCreditApproved(final boolean approved, UUID correlationId) {
        try {
            final String message = objectMapper.writeValueAsString(approved);
            log.info("message = {}", message);
            ProducerRecord<String, String> record = new ProducerRecord<>("credit.approved.response", message);
            record.headers().add("correlation_id",(String.valueOf(correlationId).getBytes()));
            record.headers().add("timeoutExpire", "30".getBytes());
            kafkaTemplate.send(record);
            log.info("Sent CREDIT_AAPROVED event: {}", message);
        } catch (final Exception e) {
            log.error("Error sending CREDIT_APRROVED event", e);
        }
    }

    public void sendCreditAccountCreatedEvent(final CreditContract creditContract, final Account creditAccount) {
        try {
            final CreditAccountCreatedResponse event = new CreditAccountCreatedResponse(
                    creditContract.getCreditContractId(),
                    creditAccount.getId(),
                    String.valueOf(creditContract.getCreditAmount())
            );
            final String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("credit.account.created.response", message);
            log.info("Sent CREDIT_ACCOUNT_CREATED event: {}", event);
        } catch (final Exception e) {
            log.error("Error sending CREDIT_ACCOUNT_CREATED event", e);
        }
    }

    public void sendCreditPaymentResponse(final CreditPaymentResponseDTO response, final UUID correlationId) {
        try {
            final String message = objectMapper.writeValueAsString(response);
            kafkaTemplate.send("credit.payment.response", message);
            log.info("Sent CREDIT_PAYMENT_RESPONSE event: {}", response);
        } catch (final Exception e) {
            log.error("Error sending CREDIT_PAYMENT_RESPONSE event", e);
        }

    }
}
