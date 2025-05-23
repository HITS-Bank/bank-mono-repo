package com.bank.hits.bankcreditservice.service.impl;

import com.bank.hits.bankcreditservice.model.CreditHistory;
import com.bank.hits.bankcreditservice.model.DTO.*;
import com.bank.hits.bankcreditservice.repository.CreditHistoryRepository;
import com.bank.hits.bankcreditservice.service.api.CreditPaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
@RequiredArgsConstructor
public class CreditPaymentServiceImpl implements CreditPaymentService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final CreditHistoryRepository creditHistoryRepository;
    private final ObjectMapper objectMapper;

    private final Map<String, SemaphoreResponsePair> semaphoreMap = new ConcurrentHashMap<>();

    private final Map<Integer, UUID> hashCodeIdempotencyMap = new ConcurrentHashMap<>();

    @Value("${kafka.topics.credit-payment.request}")
    private String creditPaymentRequestTopic;

    @Value("${kafka.topics.credit-payment.response}")
    private String creditPaymentResponseTopic;

    @Override
    @Transactional
    public boolean processPayment(UUID loanId,CreditPaymentRequestDTO request, PaymentStatus paymentStatus) throws Exception {
        CreditHistory creditHistory = creditHistoryRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Кредит не найден"));

        return processPaymentWithResilience(creditHistory, request, paymentStatus);
    }


    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentService")
    private boolean processPaymentWithResilience(CreditHistory creditHistory, CreditPaymentRequestDTO dto, PaymentStatus status) throws Exception {
        int hashCode = creditHistory.getId().hashCode();
        hashCode= 31 * hashCode + dto.getAmount().hashCode();
        hashCode = 31 * hashCode + status.hashCode();
        hashCode = 31 * hashCode + creditHistory.getRemainingDebt().hashCode();

        UUID requestId;
        if (hashCodeIdempotencyMap.containsKey(hashCode)) {
            requestId = hashCodeIdempotencyMap.get(hashCode);
        } else {
            requestId = UUID.randomUUID();
            hashCodeIdempotencyMap.put(hashCode, requestId);
        }

        String correlationId = sendPaymentRequest(creditHistory.getId(), dto.getAmount(), status, creditHistory.getRemainingDebt(), requestId);
        Semaphore sem = semaphoreMap.get(correlationId).getSemaphore();
        if (!sem.tryAcquire(30, TimeUnit.SECONDS))
            throw new RuntimeException("Timeout waiting for credit payment response");

        SemaphoreResponsePair pair = semaphoreMap.remove(correlationId);
        CreditPaymentResponseDTO resp = objectMapper.readValue(pair.getResponse(), CreditPaymentResponseDTO.class);
        hashCodeIdempotencyMap.remove(hashCode);
        if (resp.isApproved()) {
            creditHistory.setRemainingDebt(creditHistory.getRemainingDebt().subtract(resp.getApprovedAmount()));
            creditHistoryRepository.save(creditHistory);
            return true;
        }
        return false;
    }

    private boolean paymentFallback(CreditHistory history, CreditPaymentRequestDTO dto, PaymentStatus status, Throwable t) {
        log.error("Платёжный сервис временно недоступен: {}", t.getMessage());
        throw new RuntimeException("Не удалось обработать платёж, сервис недоступен", t);
    }

    private String sendPaymentRequest(UUID creditId, BigDecimal amount, PaymentStatus paymentStatus, BigDecimal remainingAmount, UUID requestId) {
        log.info("pay started");
        String correlationId = UUID.randomUUID().toString();
        try {
            CreditRepaymentRequest paymentDTO = new CreditRepaymentRequest();
            paymentDTO.setCreditAmount(amount.toString());
            paymentDTO.setCreditContractId(creditId);
            paymentDTO.setEnrollmentDate(LocalDateTime.now());
            paymentDTO.setPaymentStatus(paymentStatus);
            paymentDTO.setRemainingAmount(remainingAmount);
            log.info("creditContractID: {} ", paymentDTO.getCreditContractId());
            String message = objectMapper.writeValueAsString(paymentDTO);
            log.info("message при отправке оплаты: {}", message);
            ProducerRecord<String, String> record = new ProducerRecord<>(creditPaymentRequestTopic, message);
            record.headers().add("correlation_id", correlationId.getBytes());
            record.headers().add("request_id", requestId.toString().getBytes());
            kafkaTemplate.send(record);
            log.info("Отправлен запрос на оплату кредита: {}", message);
            semaphoreMap.put(correlationId, new SemaphoreResponsePair(new Semaphore(0), null));
            log.info("CORRELATION_ID: {}", correlationId);
        } catch (JsonProcessingException e) {
            log.error("Ошибка при сериализации CreditPaymentProcessingDTO", e);
        }
        return correlationId;
    }


    @KafkaListener(topics = "${kafka.topics.credit-payment.response}", groupId = "creditPaymentGroup")
    public void receivePaymentResponse(ConsumerRecord<String, String> record) {
        log.info("сообщение получено");
        Header header = record.headers().lastHeader("correlation_id");
        if (header == null) {
            log.warn("Получен ответ без заголовка correlation_id");
            return;
        }
        String correlationId = new String(header.value());
        String response = record.value();

        log.info("ПРИШЁЛ CORRELATION_ID {}", correlationId);
        SemaphoreResponsePair pair = semaphoreMap.get(correlationId);
        if (pair != null) {
            pair.setResponse(response);
            pair.getSemaphore().release();
        } else {
            log.warn("Не найден ожидающий поток для correlationId: " + correlationId);
        }
    }


    @Scheduled(fixedRate = 60000)
    public void processScheduledPayments() {
        log.info("Автоматическая обработка платежей по кредитам...");

        List<CreditHistory> activeCredits = creditHistoryRepository.findByRemainingDebtGreaterThan(BigDecimal.ZERO);

        for (CreditHistory credit : activeCredits) {
            try {
                log.info("Обрабатываем автоматический платеж для кредита {}", credit.getNumber());

                CreditPaymentRequestDTO request = new CreditPaymentRequestDTO();
                request.setAmount(credit.getMonthlyPayment());

                boolean success = processPayment(credit.getId(),request, PaymentStatus.PLANNED);

                if (success) {
                    log.info("Платёж для кредита {} успешно проведён", credit.getNumber());
                } else {
                    log.warn("Платёж для кредита {} не прошел", credit.getNumber());
                }
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                log.error("Ошибка при обработке кредита {}: {}", credit.getNumber(), e.getMessage());
            }
        }
    }

    private static class SemaphoreResponsePair {
        private final Semaphore semaphore;
        private String response;

        public SemaphoreResponsePair(Semaphore semaphore, String response) {
            this.semaphore = semaphore;
            this.response = response;
        }

        public Semaphore getSemaphore() {
            return semaphore;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }
}
