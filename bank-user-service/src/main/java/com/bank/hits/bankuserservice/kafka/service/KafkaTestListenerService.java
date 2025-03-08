package com.bank.hits.bankuserservice.kafka.service;

import com.bank.hits.bankuserservice.kafka.message.UserUUIDMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaTestListenerService {

    @KafkaListener(topics = "user.ban.client", groupId = "test-group")
    public void listenClientBanned(ConsumerRecord<String, UserUUIDMessage> record) {
        UserUUIDMessage message = record.value();
    }

    @KafkaListener(topics = "user.unban.client", groupId = "test-group")
    public void listenClientUnbanned(ConsumerRecord<String, UserUUIDMessage> record) {
        UserUUIDMessage message = record.value();
    }

    @KafkaListener(topics = "user.ban.employee", groupId = "test-group")
    public void listenEmployeeBanned(ConsumerRecord<String, UserUUIDMessage> record) {
        UserUUIDMessage message = record.value();
    }

    @KafkaListener(topics = "user.unban.employee", groupId = "test-group")
    public void listenEmployeeUnbanned(ConsumerRecord<String, UserUUIDMessage> record) {
        UserUUIDMessage message = record.value();
    }
}
