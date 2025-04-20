package com.bank.hits.banknotificationservice.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationEntity {

    private String title;
    private String body;
}
