package com.bank.hits.bankuserservice.kafka.service;

import com.bank.hits.bankuserservice.user_service.service.KeycloakAuthService;
import com.bank.hits.bankuserservice.user_service.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.bank.hits.bankuserservice.common.dto.UserDto;
import com.bank.hits.bankuserservice.kafka.message.InformationAboutBlockingDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaListenerService {

    private final UserService userService;
    private final KeycloakAuthService keycloakAuthService;

    private final ObjectMapper objectMapper;
    private final KafkaProfileService kafkaProfileService;

    @KafkaListener(topics = "credit.user.info.request", groupId = "user-service-group")
    public void listenCreditUserInfoRequest(ConsumerRecord<String, String> record) throws JsonProcessingException {
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
            //response = responseDTO.userId();
            log.info("response= {}", response);
        } catch (Exception e) {
            log.error("Ошибка при десериализации JSON: ", e);
            return;
        }

        String adminToken = keycloakAuthService.getAdminToken();
        UserDto profile = userService.getUserProfile(adminToken, response);

        InformationAboutBlockingDTO answer = new InformationAboutBlockingDTO(profile.getIsBlocked());
        kafkaProfileService.sendUserBanned(correlationId, answer);
    }
}
