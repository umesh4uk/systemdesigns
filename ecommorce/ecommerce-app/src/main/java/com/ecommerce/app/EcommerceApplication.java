package com.ecommerce.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Application entry point.
 *
 * <p>Component scan covers all com.ecommerce.* packages, picking up
 * every bounded context automatically. No cross-context coupling at
 * the bean level — modules communicate through domain events.
 */
@SpringBootApplication(scanBasePackages = "com.ecommerce")
@EnableJpaAuditing
@EnableAsync
@ConfigurationPropertiesScan(basePackages = "com.ecommerce")
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}
