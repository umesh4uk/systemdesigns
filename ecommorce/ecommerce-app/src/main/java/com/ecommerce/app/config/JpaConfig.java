package com.ecommerce.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA configuration. Scans all bounded context repository packages.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.ecommerce")
public class JpaConfig {
    // JPA auditing is enabled via @EnableJpaAuditing on the main class.
    // All repositories are discovered by the broad basePackages scan.
}
