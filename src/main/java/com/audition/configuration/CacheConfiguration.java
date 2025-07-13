package com.audition.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.TimeUnit;

/**
 * Caching configuration using Caffeine cache implementation.
 *
 * This configuration sets up in-memory caching with the following specifications:
 * - Maximum 1000 entries per cache
 * - Expire after write: 5 minutes
 * - Expire after access: 2 minutes
 * - Statistics recording enabled for monitoring
 *
 * Predefined cache names:
 * - posts - for all posts data
 * - posts-with-comments - for posts with embedded comments
 * - comments - for standalone comments
 *
 * @author Farhan Rayani
 */

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Configure Caffeine cache with TTL and maximum size
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000) // Maximum 1000 entries per cache
                .expireAfterWrite(5, TimeUnit.MINUTES) // Expire after 5 minutes
                .expireAfterAccess(2, TimeUnit.MINUTES) // Expire after 2 minutes of no access
                .recordStats() // Enable cache statistics for monitoring
        );

        // Define cache names
        cacheManager.setCacheNames(java.util.List.of(
                "posts",
                "posts-with-comments",
                "comments"
        ));

        return cacheManager;
    }
}