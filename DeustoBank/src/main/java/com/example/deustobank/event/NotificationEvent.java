package com.example.deustobank.event;

import org.springframework.context.ApplicationEvent;

public class NotificationEvent extends ApplicationEvent {

    private final Long userId;
    private final String message;
    private final String type;

    public NotificationEvent(Object source, Long userId, String message, String type) {
        super(source);
        this.userId = userId;
        this.message = message;
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }
}
