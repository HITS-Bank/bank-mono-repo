package com.bank.hits.bankcreditservice.service.impl;

import com.bank.hits.bankcreditservice.model.CreditHistory;
import com.bank.hits.bankcreditservice.model.DTO.*;
import com.bank.hits.bankcreditservice.repository.CreditHistoryRepository;
import com.bank.hits.bankcreditservice.service.api.CreditPaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Value("${kafka.topics.credit-payment.request}")
    private String creditPaymentRequestTopic;

    @Value("${kafka.topics.credit-payment.response}")
    private String creditPaymentResponseTopic;

    @Override
    @Transactional
    public boolean processPayment(CreditPaymentRequestDTO request, PaymentStatus paymentStatus) throws Exception {
        CreditHistory creditHistory = creditHistoryRepository.findByNumber(request.getLoanNumber())
                .orElseThrow(() -> new RuntimeException("Кредит с таким номером не найден"));
        String correlationId = sendPaymentRequest(creditHistory.getId(), request.getPaymentAmount(), paymentStatus);
        Semaphore semaphore = new Semaphore(0);
        semaphoreMap.put(correlationId, new SemaphoreResponsePair(semaphore, null));
        boolean acquired = semaphore.tryAcquire(30, TimeUnit.SECONDS);
        if (!acquired) {
            throw new RuntimeException("Timeout waiting for credit payment response");
        }
        SemaphoreResponsePair pair = semaphoreMap.remove(correlationId);
        if (pair == null || pair.getResponse() == null) {
            throw new RuntimeException("No valid response received for credit payment");
        }

        CreditPaymentResponseDTO responseDTO = objectMapper.readValue(pair.getResponse(), CreditPaymentResponseDTO.class);
        log.info("responseDTO:", responseDTO);
        if (responseDTO.isApproved()) {
            creditHistory.setRemainingDebt(creditHistory.getRemainingDebt().subtract(responseDTO.getApprovedAmount()));
            creditHistoryRepository.save(creditHistory);
            return true;
        } else {
            return false;
        }
    }

    private String sendPaymentRequest(UUID creditId, BigDecimal amount, PaymentStatus paymentStatus) {
        log.info("pay started");
        String correlationId = UUID.randomUUID().toString();
        try {
            CreditRepaymentRequest paymentDTO = new CreditRepaymentRequest();
            paymentDTO.setCreditAmount(amount.toString());
            paymentDTO.setCreditContractId(creditId);
            paymentDTO.setEnrollmentDate(LocalDateTime.now());
            paymentDTO.setPaymentStatus(paymentStatus);
            String message = objectMapper.writeValueAsString(paymentDTO);
            log.info("message при отправке оплаты: {}", message);
            ProducerRecord<String, String> record = new ProducerRecord<>(creditPaymentRequestTopic, message);
            record.headers().add("correlation_id", correlationId.getBytes());
            kafkaTemplate.send(record);
            log.info("Отправлен запрос на оплату кредита: {}", message);
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
                request.setLoanNumber(credit.getNumber());
                request.setPaymentAmount(credit.getMonthlyPayment());

                boolean success = processPayment(request, PaymentStatus.PLANNED);

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
