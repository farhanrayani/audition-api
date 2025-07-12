package com.audition;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.sleuth.enabled=false",
        "management.tracing.enabled=false"
})
class AuditionApplicationTests {

    @Test
    void contextLoads() {
        // Test that the Spring Boot application context loads successfully
        // This is a basic smoke test to ensure all beans are properly configured
    }

    @Test
    void applicationStarts() {
        // Additional test to verify the application can start without errors
        // The @SpringBootTest annotation will start the full application context
    }
}