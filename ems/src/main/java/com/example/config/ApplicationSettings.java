package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationSettings {

    @Value("${company.name}")
    private String companyName;

    @Value("${company.currency}")
    private String companyCurrency;

    @Value("${notification.retry-count}")
    private int notificationRetryCount;

    @Value("${raise.max-percentage}")
    private double maxRaisePercentage;

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyCurrency() {
        return companyCurrency;
    }

    public int getNotificationRetryCount() {
        return notificationRetryCount;
    }

    public double getMaxRaisePercentage() {
        return maxRaisePercentage;
    }
}
