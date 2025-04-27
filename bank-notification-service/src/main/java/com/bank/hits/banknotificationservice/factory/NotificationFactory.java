package com.bank.hits.banknotificationservice.factory;

import com.bank.hits.banknotificationservice.model.AccountOperationEvent;
import com.bank.hits.banknotificationservice.model.CurrencyCode;
import com.bank.hits.banknotificationservice.model.NotificationEntity;
import com.bank.hits.banknotificationservice.model.OperationType;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
public class NotificationFactory {

    public NotificationEntity createNotification(AccountOperationEvent event) {
        return NotificationEntity.builder()
                .title(getNotificationTitle(event.getOperationType()))
                .body(getNotificationBody(event))
                .build();
    }

    private String getNotificationTitle(OperationType operationType) {
        return switch (operationType) {
            case TOP_UP -> "Пополнение счета";
            case WITHDRAW -> "Вывод средств";
            case LOAN_PAYMENT -> "Платеж по кредиту";
            case TRANSFER_INCOMING -> "Входящий перевод";
            case TRANSFER_OUTGOING -> "Исходящий перевод";
        };
    }

    private String getNotificationBody(
            AccountOperationEvent event
    ) {
        String amount = event.getOperationAmount().toString();
        String currencySymbol = getCurrencySymbol(event.getCurrencyCode());
        String balance = event.getAccountBalance().toString();
        return switch (event.getOperationType()) {
            case TOP_UP ->
                    MessageFormat.format("Пополнение на {0} {1}. Баланс: {2} {1}", amount, currencySymbol, balance);
            case WITHDRAW ->
                    MessageFormat.format("Вывод {0} {1}. Баланс: {2} {1}", amount, currencySymbol, balance);
            case LOAN_PAYMENT ->
                    MessageFormat.format("Платеж по кредиту на {0} {1}. Баланс: {2} {1}", amount, currencySymbol, balance);
            case TRANSFER_INCOMING ->
                    MessageFormat.format("Входящий перевод на {0} {1}. Баланс: {2} {1}", amount, currencySymbol, balance);
            case TRANSFER_OUTGOING ->
                    MessageFormat.format("Исходящий перевод на {0} {1}. Баланс: {2} {1}", amount, currencySymbol, balance);
        };
    }

    private String getCurrencySymbol(CurrencyCode currencyCode) {
        return switch (currencyCode) {
            case RUB -> "₽";
            case KZT -> "₸";
            case CNY -> "¥";
        };
    }
}
