package com.example.notifiy;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class SmsNotifier implements Notifier {
    @Override
    public void notify(String message) {
        System.out.println("Notifying via Sms: " + message);
    }
}
