package com.bank.hits.banknotificationservice.service;

import com.bank.hits.banknotificationservice.repository.DeviceTokenRepository;
import com.bank.hits.banknotificationservice.model.DeviceTokenEntity;
import lombok.RequiredArgsConstructor;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository repository;

    public String getFcmTokenByUserId(String userId) {
        return repository
                .findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Не найден fcmToken для userId " + userId))
                .getFcmToken();
    }

    public void saveOrUpdateToken(String userToken, String fcmToken) {
        String userId = getUserId(userToken);
        Optional<DeviceTokenEntity> existingDeviceToken = repository.findByUserId(userId);
        if (existingDeviceToken.isPresent()) {
            DeviceTokenEntity token = existingDeviceToken.get();
            token.setFcmToken(fcmToken);
            repository.save(token);
        } else {
            DeviceTokenEntity newToken = new DeviceTokenEntity();
            newToken.setUserId(userId);
            newToken.setFcmToken(fcmToken);
            repository.save(newToken);
        }
    }

    private String getUserId(String userToken) {
        try {
            return TokenVerifier.create(userToken, AccessToken.class).getToken().getSubject();
        } catch (VerificationException e) {
            throw new RuntimeException(e);
        }
    }
}
