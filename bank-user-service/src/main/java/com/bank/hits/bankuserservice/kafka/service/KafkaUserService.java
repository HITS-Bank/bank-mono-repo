package com.bank.hits.bankuserservice.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.bank.hits.bankuserservice.kafka.message.InformationAboutBlockingDTO;
import com.bank.hits.bankuserservice.kafka.message.UserUUIDMessage;

import java.text.MessageFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaUserService {

    private final String BAN_ACTION_TOPIC_PART = "ban";
    private final String UNBAN_ACTION_TOPIC_PART = "unban";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendUserBanned(String correlationId, InformationAboutBlockingDTO message) throws JsonProcessingException {
        final String topic = "credit.user.info.response";
        sendMessageWithCorrelationId(topic, correlationId, message);
    }


    public void coreSendUserBanned(String userId) throws JsonProcessingException {
        final String topic = getTopicName(BAN_ACTION_TOPIC_PART);
        UserUUIDMessage message = new UserUUIDMessage(userId);
        kafkaTemplate.send(topic, objectMapper.writeValueAsString(message));
    }

    public void coreSendUserUnbanned(String userId) throws JsonProcessingException {
        final String topic = getTopicName(UNBAN_ACTION_TOPIC_PART);
        UserUUIDMessage message = new UserUUIDMessage(userId);
        kafkaTemplate.send(topic, objectMapper.writeValueAsString(message));
    }

    private String getTopicName(String action) {
        final String SERVICE_NAME_TOPIC_PART = "user";
        return MessageFormat.format("{0}.{1}", SERVICE_NAME_TOPIC_PART, action);
    }

    private void sendMessageWithCorrelationId(String topic, String correlationId, Object messageContent) throws JsonProcessingException {
        log.info("messageContent: " + messageContent);
        String result = objectMapper.writeValueAsString(messageContent);
        log.info("result: " + result);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, result);
        record.headers().add("correlation_id", correlationId.getBytes());
        kafkaTemplate.send(record);
    }
}
