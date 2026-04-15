package com.saas.notification.domain.enums;

public enum NotificationType {
    EMAIL("EMAIL"),
    SMS("SMS"),
    PUSH("PUSH"),
    IN_APP("IN_APP");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}