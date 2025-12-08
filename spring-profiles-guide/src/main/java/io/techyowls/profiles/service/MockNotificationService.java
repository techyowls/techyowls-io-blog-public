package io.techyowls.profiles.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Mock service for dev/test - no real emails sent.
 */
@Service
@Profile({"dev", "test", "default"})
@Slf4j
public class MockNotificationService implements NotificationService {

    @Override
    public void send(String to, String message) {
        // Just log, don't send real emails in dev
        log.info("MOCK EMAIL (not sent) to {}: {}", to, message);
    }

    @Override
    public String getServiceName() {
        return "MockNotificationService (Dev/Test)";
    }
}
