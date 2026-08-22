package com.ecommerce.app.dashboard;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.util.Map;

/**
 * Cache configuration for the dashboard.
 *
 * <p>Uses a dedicated Redis cache named {@code "dashboard"} with a 60-second TTL.
 * A scheduled task evicts the cache every 55 seconds so the first request after
 * each window gets a fresh result — avoiding stale data piling up.
 */
@Configuration
@EnableScheduling
public class DashboardCacheConfig {

    /** TTL for the dashboard cache. */
    public static final Duration DASHBOARD_TTL = Duration.ofSeconds(60);

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(10));    // fallback for other caches

        RedisCacheConfiguration dashboardConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(DASHBOARD_TTL);

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("dashboard", dashboardConfig)
                .transactionAware()
                .build();
    }

    /** Pro-actively evict the dashboard cache every 55 seconds. */
    @Scheduled(fixedDelay = 55_000)
    @CacheEvict(value = "dashboard", allEntries = true)
    public void evictDashboardCache() {
        // eviction handled by @CacheEvict — method body intentionally empty
    }
}
