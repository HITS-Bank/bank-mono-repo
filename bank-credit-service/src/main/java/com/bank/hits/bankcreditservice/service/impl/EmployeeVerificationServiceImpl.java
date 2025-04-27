package com.bank.hits.bankcreditservice.service.impl;

import com.bank.hits.bankcreditservice.model.DTO.VerificationAnswerDTO;
import com.bank.hits.bankcreditservice.model.DTO.VerificationResponseDTO;
import com.bank.hits.bankcreditservice.service.api.EmployeeVerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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
        Semaphore semaphore = new Semaphore(0);
        return verifyWithResilience(employeeUuid,semaphore);
    }



    @CircuitBreaker(name = "employeeVerificationService", fallbackMethod = "verificationFallback")
    @Retry(name = "employeeVerificationService")
    private boolean verifyWithResilience(String employeeUuid, Semaphore semaphore) throws Exception {
        String correlationId = sendVerificationMessage(employeeUuid,employeeUuid,semaphore);
        Semaphore sem = semaphoreMap.get(correlationId).getSemaphore();
        if (!sem.tryAcquire(30, TimeUnit.SECONDS))
            throw new RuntimeException("Timeout waiting for employee verification response");

        SemaphoreResponsePair pair = semaphoreMap.remove(correlationId);
        VerificationAnswerDTO answer = objectMapper.readValue(pair.getResponse(), VerificationAnswerDTO.class);
        return !answer.isBlocked();
    }

    private boolean verificationFallback(String employeeUuid, Throwable t) {
        log.error("Сервис верификации сотрудников недоступен: {}", t.getMessage());
        throw new RuntimeException("Не удалось проверить сотрудника, сервис недоступен", t);
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
