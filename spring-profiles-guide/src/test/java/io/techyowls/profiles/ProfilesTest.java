package io.techyowls.profiles;

import io.techyowls.profiles.config.AppConfig;
import io.techyowls.profiles.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ProfilesTest {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private NotificationService notificationService;

    @Test
    void testProfileConfigIsLoaded() {
        assertThat(appConfig.getEnvironment()).isEqualTo("test");
        assertThat(appConfig.isDebugMode()).isTrue();
        assertThat(appConfig.getCache().isEnabled()).isFalse();
        assertThat(appConfig.getEmail().isSendEnabled()).isFalse();
    }

    @Test
    void testCorrectServiceIsInjected() {
        // Test profile should get MockNotificationService
        assertThat(notificationService.getServiceName()).contains("Mock");
    }
}
