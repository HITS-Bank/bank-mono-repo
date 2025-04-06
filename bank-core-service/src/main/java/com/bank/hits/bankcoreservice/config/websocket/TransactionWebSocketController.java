package com.bank.hits.bankcoreservice.config.websocket;

import com.bank.hits.bankcoreservice.api.dto.AccountTransactionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Slf4j
@Controller
public class TransactionWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public TransactionWebSocketController(final SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendTransactionUpdate(final UUID accountId, final AccountTransactionDto transaction) {
        final String destination = "/topic/bank_account/" + accountId + "/operation_history";
        log.info("Sending transaction update to destination: {}", destination);
        log.info("Transaction details: {}", transaction);
        messagingTemplate.convertAndSend(destination, transaction);
    }
}
