package com.bank.hits.bankuserservice.kafka.service;

import com.bank.hits.bankuserservice.kafka.message.UserUUIDMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaTestListenerService {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user.ban.client", groupId = "test-group")
    public void listenClientBanned(ConsumerRecord<String, String> record) {
        //UserUUIDMessage message = record.value();
    }

    @KafkaListener(topics = "user.unban.client", groupId = "test-group")
    public void listenClientUnbanned(ConsumerRecord<String, String> record) {
        //UserUUIDMessage message = record.value();
    }

    @KafkaListener(topics = "user.ban.employee", groupId = "test-group")
    public void listenEmployeeBanned(ConsumerRecord<String, String> record) {
        //UserUUIDMessage message = record.value();
    }

    @KafkaListener(topics = "user.unban.employee", groupId = "test-group")
    public void listenEmployeeUnbanned(ConsumerRecord<String, String> record) {
        //UserUUIDMessage message = record.value();
    }
}
