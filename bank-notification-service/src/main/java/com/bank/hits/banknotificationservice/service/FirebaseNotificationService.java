package com.bank.hits.banknotificationservice.service;

import com.bank.hits.banknotificationservice.model.NotificationEntity;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FirebaseNotificationService {

    public void sendNotification(Optional<String> fcmToken, NotificationEntity notification) {
        Message.Builder messageBuilder = Message.builder()
                .setNotification(
                        Notification.builder()
                                .setTitle(notification.getTitle())
                                .setBody(notification.getBody())
                                .build()
                );

        if (fcmToken.isPresent()) {
            messageBuilder.setToken(fcmToken.get());
        } else {
            messageBuilder.setTopic("operations");
        }

        Message message = messageBuilder.build();
        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }
}
