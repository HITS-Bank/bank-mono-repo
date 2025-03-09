package com.bank.hits.bankuserservice.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.bank.hits.bankuserservice.common.dto.UserDto;
import com.bank.hits.bankuserservice.kafka.message.CreditUserInfoRequestPayload;
import com.bank.hits.bankuserservice.kafka.message.InformationAboutBlockingDTO;
import com.bank.hits.bankuserservice.profile.service.ProfileService;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaListenerService {

    private final ProfileService profileService;

    private final ObjectMapper objectMapper;
    private final KafkaProfileService kafkaProfileService;

    @KafkaListener(topics = "credit.user.info.request", groupId = "user-service-group")
    public void listenCreditUserInfoRequest(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader("correlation_id");
        if (header == null) {
            log.warn("Получено сообщение без заголовка correlation_id");
            return;
        }
        String correlationId = new String(header.value());

        log.info("Получено сообщение с corId {}", correlationId);
        Object messageValue = record.value();
        String response;

        if (messageValue instanceof String) {
            log.info("строка");
            response = (String) messageValue;
            log.info("response= {}", response);
        } else {
            log.info("объект");
            try {
                CreditUserInfoRequestPayload responseDTO = objectMapper.convertValue(messageValue, CreditUserInfoRequestPayload.class);
                response = responseDTO.getUserId();
                log.info("response= {}", response);
            } catch (Exception e) {
                log.error("Ошибка при десериализации JSON: ", e);
                return;
            }
        }


        UserDto profile = profileService.getSelfProfile(UUID.fromString(response));

        InformationAboutBlockingDTO answer = new InformationAboutBlockingDTO(profile.getIsBanned());
        kafkaProfileService.sendUserBanned(correlationId, answer);
    }
}
