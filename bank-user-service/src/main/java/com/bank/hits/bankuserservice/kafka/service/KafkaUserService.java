package com.bank.hits.bankuserservice.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import com.bank.hits.bankuserservice.kafka.message.InformationAboutBlockingDTO;
import com.bank.hits.bankuserservice.kafka.message.UserUUIDMessage;

import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaUserService {

    private final String BAN_ACTION_TOPIC_PART = "ban";
    private final String UNBAN_ACTION_TOPIC_PART = "unban";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @CircuitBreaker(name = "kafkaUserService", fallbackMethod = "sendUserBannedFallback")
    @Retry(name = "kafkaUserService")
    public void sendUserBanned(String correlationId, InformationAboutBlockingDTO message) throws JsonProcessingException {
        final String topic = "credit.user.info.response";
        sendMessageWithCorrelationId(topic, correlationId, message);
    }

    private void sendUserBannedFallback(String correlationId,
                                        InformationAboutBlockingDTO message,
                                        Throwable t) {
        log.error("FALLBACK sendUserBanned – correlationId={}, message={} – error: {}",
                correlationId, message, t.toString());
    }

    @CircuitBreaker(name = "kafkaUserService", fallbackMethod = "coreSendUserBannedFallback")
    @Retry(name = "kafkaUserService")
    public void coreSendUserBanned(String userId) throws JsonProcessingException {
        final String topic = getTopicName(BAN_ACTION_TOPIC_PART);
        UserUUIDMessage message = new UserUUIDMessage(userId);
        sendMessage(topic, objectMapper.writeValueAsString(message));
    }

    private void coreSendUserBannedFallback(String userId, Throwable t) {
        log.error("FALLBACK coreSendUserBanned – userId={} – error: {}",
                userId, t.toString());
    }

    @CircuitBreaker(name = "kafkaUserService", fallbackMethod = "coreSendUserUnbannedFallback")
    @Retry(name = "kafkaUserService")

    public void coreSendUserUnbanned(String userId) throws JsonProcessingException {
        final String topic = getTopicName(UNBAN_ACTION_TOPIC_PART);
        UserUUIDMessage message = new UserUUIDMessage(userId);
        sendMessage(topic, objectMapper.writeValueAsString(message));
    }

    private void coreSendUserUnbannedFallback(String userId, Throwable t) {
        log.error("FALLBACK coreSendUserUnbanned – userId={} – error: {}",
                userId, t.toString());
    }
    private String getTopicName(String action) {
        final String SERVICE_NAME_TOPIC_PART = "user";
        return MessageFormat.format("{0}.{1}", SERVICE_NAME_TOPIC_PART, action);
    }

    private void sendMessage(String topic, String payload) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, payload);
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(record);
        try {
            future.get(5, TimeUnit.SECONDS);
            log.info("Sent to {} – payload={}", topic, payload);
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка отправки в Kafka topic=" + topic, ex);
        }
    }

    private void sendMessageWithCorrelationId(String topic,
                                              String correlationId,
                                              Object messageContent) throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(messageContent);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, payload);
        record.headers().add("correlation_id", correlationId.getBytes());

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(record);
        try {
            future.get(5, TimeUnit.SECONDS);
            log.info("Sent to {} – correlationId={}, payload={}", topic, correlationId, payload);
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Ошибка отправки в Kafka topic=" + topic + ", correlationId=" + correlationId, ex);
        }
    }
}
