package com.bank.hits.bankuserservice.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
    private final KafkaProfileService kafkaProfileService;

    @KafkaListener(topics = "credit.user.info.request", groupId = "user-service-group")
    public void listenCreditUserInfoRequest(ConsumerRecord<String, Object> record) {
        String correlationId = new String((record.headers().lastHeader("correlation_id").value()));
        CreditUserInfoRequestPayload message = (CreditUserInfoRequestPayload) record.value();

        UserDto profile = profileService.getSelfProfile(UUID.fromString(message.userId()));

        InformationAboutBlockingDTO answer = new InformationAboutBlockingDTO(profile.getIsBanned());
        kafkaProfileService.sendUserBanned(correlationId, answer);
    }
}
