package com.example.notifiy;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class PushNotifier implements Notifier {
    @Override
    public void notify(String message) {
        System.out.println("Notifying via Push Notifications: " + message);
    }
}
