package com.bank.hits.bankcoreservice.core.utils;

import java.time.LocalDateTime;
import java.util.Random;

public class ExceptionUtils {

    private static final Random RANDOM = new Random();

    public static void throwExceptionRandomly() {
        int random = 0;
        if (LocalDateTime.now().getMinute() % 2 == 0) {
            random = RANDOM.nextInt(10);
        } else {
            random = RANDOM.nextInt(2);
        }
        if (random != 0) {
            throw new RuntimeException("Random exception");
        }
    }
}
