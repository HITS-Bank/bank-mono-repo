package com.bank.hits.banknotificationservice.kafka;

import com.bank.hits.banknotificationservice.factory.NotificationFactory;
import com.bank.hits.banknotificationservice.model.AccountOperationEvent;
import com.bank.hits.banknotificationservice.model.NotificationEntity;
import com.bank.hits.banknotificationservice.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountOperationsConsumer {

    private final NotificationFactory notificationFactory;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "core.operations", groupId = "bank.group")
    public void listenAccountOperations(ConsumerRecord<String, String> record) throws JsonProcessingException {
        AccountOperationEvent event = objectMapper.readValue(record.value(), AccountOperationEvent.class);
        log.info("Received account operation event: {}", event);

        NotificationEntity notification = notificationFactory.createNotification(event);
        log.info("Created notification model: {}", notification);

        try {
            notificationService.sendToClient(event.getUserId(), notification);
            log.info("Sent push notification to client with userId: {}", event.getUserId());

            notificationService.broadcast(notification);
            log.info("Sent broadcast push notification");
        } catch (Exception e) {
            log.error("Error processing operation event");
        }
    }
}
