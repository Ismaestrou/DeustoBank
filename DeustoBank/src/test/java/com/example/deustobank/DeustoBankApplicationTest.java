package com.example.deustobank;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test that verifies the Spring context loads and covers
 * DeustoBankApplication.main() via SpringBootTest.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class DeustoBankApplicationTest {

    @Test
    void contextLoads() {
        // Spring context starts successfully — verifies the application wires up correctly
    }

    @Test
    void main_DoesNotThrow() {
        // Covers the main() entry point directly
        // Using a no-op args array since the context is already managed by @SpringBootTest
        assertDoesNotThrow(() ->
            DeustoBankApplication.main(new String[]{
                "--spring.datasource.url=jdbc:h2:mem:testdb2",
                "--spring.datasource.driver-class-name=org.h2.Driver",
                "--spring.datasource.username=sa",
                "--spring.datasource.password=",
                "--spring.jpa.hibernate.ddl-auto=create-drop",
                "--spring.main.web-application-type=none"
            })
        );
    }
}