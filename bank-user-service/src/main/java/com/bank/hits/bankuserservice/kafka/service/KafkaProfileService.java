package com.bank.hits.bankuserservice.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.bank.hits.bankuserservice.kafka.message.InformationAboutBlockingDTO;
import com.bank.hits.bankuserservice.kafka.message.UserUUIDMessage;

import java.text.MessageFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProfileService {

    private final String BAN_ACTION_TOPIC_PART = "ban";
    private final String UNBAN_ACTION_TOPIC_PART = "unban";
    private final String CLIENT_TOPIC_PART = "client";
    private final String EMPLOYEE_TOPIC_PART = "employee";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserBanned(String correlationId, InformationAboutBlockingDTO message) {
        final String topic = "credit.user.info.response";
        sendMessageWithCorrelationId(topic, correlationId, message);
    }

    public void sendClientBanned(UUID userId) {
        final String topic = getTopicName(BAN_ACTION_TOPIC_PART, CLIENT_TOPIC_PART);
        UserUUIDMessage message = new UserUUIDMessage(userId.toString());
        kafkaTemplate.send(topic, message);
    }

    public void sendEmployeeBanned(UUID userId) {
        final String topic = getTopicName(BAN_ACTION_TOPIC_PART, EMPLOYEE_TOPIC_PART);
        UserUUIDMessage message = new UserUUIDMessage(userId.toString());
        kafkaTemplate.send(topic, message);
    }

    public void sendClientUnbanned(UUID userId) {
        final String topic = getTopicName(UNBAN_ACTION_TOPIC_PART, CLIENT_TOPIC_PART);
        UserUUIDMessage message = new UserUUIDMessage(userId.toString());
        kafkaTemplate.send(topic, message);
    }

    public void sendEmployeeUnbanned(UUID userId) {
        final String topic = getTopicName(UNBAN_ACTION_TOPIC_PART, EMPLOYEE_TOPIC_PART);
        UserUUIDMessage message = new UserUUIDMessage(userId.toString());
        kafkaTemplate.send(topic, message);
    }

    private String getTopicName(String action, String role) {
        final String SERVICE_NAME_TOPIC_PART = "user";
        return MessageFormat.format("{0}.{1}.{2}", SERVICE_NAME_TOPIC_PART, action, role);
    }

    private void sendMessageWithCorrelationId(String topic, String correlationId, Object messageContent) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, messageContent);
        record.headers().add("correlation_id", correlationId.getBytes());
        kafkaTemplate.send(record);
    }
}
