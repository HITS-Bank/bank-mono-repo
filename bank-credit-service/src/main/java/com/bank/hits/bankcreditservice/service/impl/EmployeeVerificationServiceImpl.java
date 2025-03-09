package com.bank.hits.bankcreditservice.service.impl;

import com.bank.hits.bankcreditservice.model.DTO.VerificationAnswerDTO;
import com.bank.hits.bankcreditservice.model.DTO.VerificationResponseDTO;
import com.bank.hits.bankcreditservice.service.api.EmployeeVerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeVerificationServiceImpl implements EmployeeVerificationService {
    private final Map<String, SemaphoreResponsePair> semaphoreMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.employee-verification.request}")
    private String employeeVerificationRequestTopic;

    @Value("${kafka.topics.employee-verification.response}")
    private String employeeVerificationResponseTopic;

    @Override
    public boolean verifyEmployee(String employeeUuid) throws Exception {
        String verificationMessage = employeeUuid;
        Semaphore semaphore = new Semaphore(0);
        log.info("Отправка сообщения верификации");
        String messageId = sendVerificationMessage(verificationMessage, employeeUuid, semaphore);
        boolean acquired = semaphore.tryAcquire(30, TimeUnit.SECONDS);
        if (!acquired) {
            throw new RuntimeException("Timeout waiting for employee verification response");
        }
        SemaphoreResponsePair pair = semaphoreMap.remove(messageId);
        if (pair != null && pair.getResponse() != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                VerificationAnswerDTO responseDTO = objectMapper.readValue(pair.getResponse(), VerificationAnswerDTO.class);
                if (!responseDTO.isBlocked()) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                log.error("Ошибка при десериализации JSON: ", e);
            }
        }
        throw new RuntimeException("No valid response received for employee verification");
    }

    private String sendVerificationMessage(String messageContent, String employeeUuid, Semaphore semaphore) throws JMSException {
        String correlationId = UUID.randomUUID().toString();
        log.info("Отправляем сообщение {}", messageContent);
        ProducerRecord<String, String> record = new ProducerRecord<>(employeeVerificationRequestTopic, messageContent);
        record.headers().add("event_type", "verify_employee".getBytes());
        record.headers().add("employee_uuid", employeeUuid.getBytes());
        record.headers().add("correlation_id", correlationId.getBytes());
        record.headers().add("timeoutExpire", "30".getBytes());
        kafkaTemplate.send(record);
        semaphoreMap.put(correlationId, new SemaphoreResponsePair(semaphore, null));
        return correlationId;
    }

    @KafkaListener(topics = "${kafka.topics.employee-verification.response}", groupId = "employeeVerificationGroup")
    public void receiveVerificationResponse(ConsumerRecord<String, String> record) throws JMSException {
        Header header = record.headers().lastHeader("correlation_id");
        if (header == null) {
            log.warn("Получено сообщение без заголовка correlation_id");
            return;
        }
        String correlationId = new String(header.value());

        log.info("Получено сообщение с corId {}", correlationId);
        Object messageValue = record.value();
        String response;

            try {
                response = objectMapper.convertValue(messageValue, String.class);
            } catch (Exception e) {
                log.error("Ошибка при десериализации JSON: ", e);
                return;
            }

        SemaphoreResponsePair pair = semaphoreMap.get(correlationId);
        if (pair != null) {
            pair.setResponse(response);
            pair.getSemaphore().release();
        } else {
            log.warn("Не найден ожидающий поток для correlationId: " + correlationId);
        }
    }

    @Data
    @AllArgsConstructor
    public static class SemaphoreResponsePair {
        private Semaphore semaphore;
        private String response;


    }

}
