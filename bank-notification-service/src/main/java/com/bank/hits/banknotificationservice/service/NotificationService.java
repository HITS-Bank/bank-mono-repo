package com.bank.hits.banknotificationservice.service;

import com.bank.hits.banknotificationservice.model.NotificationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final DeviceTokenService deviceTokenService;
    private final FirebaseNotificationService firebaseNotificationService;

    public void sendToClient(String userId, NotificationEntity notification) {
        String fcmToken = deviceTokenService.getFcmTokenByUserId(userId);
        firebaseNotificationService.sendNotification(Optional.of(fcmToken), notification);
    }

    public void broadcast(NotificationEntity notification) {
        firebaseNotificationService.sendNotification(Optional.empty(), notification);
    }
}
