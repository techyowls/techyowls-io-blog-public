package io.techyowls.profiles.service;

/**
 * Interface for notification services.
 * Different implementations for different profiles.
 */
public interface NotificationService {

    void send(String to, String message);

    String getServiceName();
}
