package com.example.notifiy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("notificationManager")
public class NotificationManager {

    List<Notifier> notifiers;

    @Value("${notification.retry-count}")
    private int retryCount;

    public void send(String message) {
        System.out.println("Sending notifications (retry limit: " + retryCount + ")");
        notifiers.forEach(notifier -> {
            notifier.notify(message);
        });
    }

    @Autowired
    public NotificationManager(List<Notifier> notifiers) {
        this.notifiers = notifiers;
    }
}
