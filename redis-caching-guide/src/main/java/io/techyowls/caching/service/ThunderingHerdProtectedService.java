package io.techyowls.caching.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Demonstrates protection against the Thundering Herd problem.
 *
 * The Problem:
 * When a cached item expires, 1000 concurrent requests all hit the DB
 * simultaneously, potentially bringing it down.
 *
 * The Solution:
 * Only ONE request refreshes the cache. Others wait or use stale data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ThunderingHerdProtectedService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DistributedLockService lockService;

    /**
     * Get or compute with thundering herd protection.
     *
     * @param cacheKey Cache key
     * @param ttl Cache TTL
     * @param compute Function to compute the value if not cached
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrCompute(String cacheKey, Duration ttl, Supplier<T> compute) {
        // 1. Try to get from cache
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Cache HIT: {}", cacheKey);
            return (T) cached;
        }

        log.debug("Cache MISS: {}", cacheKey);

        // 2. Cache miss - acquire lock to prevent thundering herd
        String lockKey = "compute:" + cacheKey;
        return lockService.executeWithLock(
            lockKey,
            Duration.ofSeconds(30),
            () -> {
                // Double-check: another thread might have populated the cache
                Object doubleCheck = redisTemplate.opsForValue().get(cacheKey);
                if (doubleCheck != null) {
                    log.debug("Cache populated by another thread: {}", cacheKey);
                    return (T) doubleCheck;
                }

                // Compute the value
                log.info("Computing value for: {}", cacheKey);
                T value = compute.get();

                // Store in cache
                redisTemplate.opsForValue().set(cacheKey, value, ttl);
                return value;
            },
            () -> {
                // Couldn't acquire lock - wait briefly and retry from cache
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                Object retryValue = redisTemplate.opsForValue().get(cacheKey);
                if (retryValue != null) {
                    return (T) retryValue;
                }
                // Last resort: compute anyway (better than failing)
                log.warn("Fallback compute for: {}", cacheKey);
                return compute.get();
            }
        );
    }

    /**
     * Proactive cache refresh - refresh before TTL expires.
     *
     * Strategy: Store data with a "soft TTL" embedded.
     * When soft TTL expires, one request refreshes while others get stale data.
     */
    public <T> T getWithProactiveRefresh(String cacheKey, Duration softTtl,
                                          Duration hardTtl, Supplier<T> compute) {
        CacheEntry<T> entry = getCacheEntry(cacheKey);

        if (entry == null) {
            // Complete cache miss
            T value = compute.get();
            storeCacheEntry(cacheKey, value, softTtl, hardTtl);
            return value;
        }

        if (entry.isExpired()) {
            // Soft TTL expired - try to refresh in background
            if (lockService.tryLock("refresh:" + cacheKey, Duration.ofSeconds(30))) {
                // Got the lock - refresh asynchronously
                // In production, use @Async or executor
                try {
                    T freshValue = compute.get();
                    storeCacheEntry(cacheKey, freshValue, softTtl, hardTtl);
                    return freshValue;
                } finally {
                    lockService.unlock("refresh:" + cacheKey);
                }
            }
        }

        // Return current value (fresh or stale)
        return entry.value();
    }

    @SuppressWarnings("unchecked")
    private <T> CacheEntry<T> getCacheEntry(String key) {
        return (CacheEntry<T>) redisTemplate.opsForValue().get("entry:" + key);
    }

    private <T> void storeCacheEntry(String key, T value, Duration softTtl, Duration hardTtl) {
        CacheEntry<T> entry = new CacheEntry<>(value, System.currentTimeMillis() + softTtl.toMillis());
        redisTemplate.opsForValue().set("entry:" + key, entry, hardTtl);
    }

    public record CacheEntry<T>(T value, long expiresAt) {
        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
