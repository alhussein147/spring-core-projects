package com.example.audit;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Scope("prototype")
public class AuditLogger {


    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
    private final int instanceId;

    public AuditLogger() {
        instanceId = INSTANCE_COUNTER.incrementAndGet();
        System.out.println("AuditLogger created: instance " + instanceId);
    }

    public void log(String message) {
        System.out.println(
                "[" + LocalDateTime.now() + "] " +
                        "AuditLogger #" + instanceId + ": " + message
        );
    }

}
