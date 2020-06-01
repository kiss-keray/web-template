package com.keray.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


public class UUIDUtil {
    private UUIDUtil() {
    }

    public static String generateUUIDByTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + UUID.randomUUID();
    }

    public static void main(String[] args) {
        System.out.println(generateUUIDByTimestamp().length());
    }
}
