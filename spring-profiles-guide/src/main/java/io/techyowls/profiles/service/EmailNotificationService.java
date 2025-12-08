package io.techyowls.profiles.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Real email service - only active in production.
 */
@Service
@Profile("prod")
@Slf4j
public class EmailNotificationService implements NotificationService {

    @Override
    public void send(String to, String message) {
        // Real email sending logic
        log.info("SENDING REAL EMAIL to {}: {}", to, message);
    }

    @Override
    public String getServiceName() {
        return "EmailNotificationService (Production)";
    }
}
