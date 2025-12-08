package io.techyowls.caching.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Distributed locking with Redis.
 *
 * Use cases:
 * - Prevent thundering herd on cache miss
 * - Ensure only one instance runs a scheduled job
 * - Rate limiting
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedLockService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Simple distributed lock.
     *
     * @param lockKey Unique key for this lock
     * @param ttl How long to hold the lock
     * @return true if lock acquired
     */
    public boolean tryLock(String lockKey, Duration ttl) {
        String key = "lock:" + lockKey;
        Boolean success = redisTemplate.opsForValue()
            .setIfAbsent(key, "locked", ttl);
        return Boolean.TRUE.equals(success);
    }

    public void unlock(String lockKey) {
        redisTemplate.delete("lock:" + lockKey);
    }

    /**
     * Execute with lock - prevents thundering herd.
     *
     * When cache misses, only ONE request queries the DB.
     * Others wait for the cache to be populated.
     */
    public <T> T executeWithLock(String lockKey, Duration lockTtl,
                                  Supplier<T> onLockAcquired,
                                  Supplier<T> onLockNotAcquired) {
        if (tryLock(lockKey, lockTtl)) {
            try {
                log.debug("Lock acquired: {}", lockKey);
                return onLockAcquired.get();
            } finally {
                unlock(lockKey);
                log.debug("Lock released: {}", lockKey);
            }
        } else {
            log.debug("Lock not acquired, using fallback: {}", lockKey);
            return onLockNotAcquired.get();
        }
    }

    /**
     * Execute with retry - waits for lock if not immediately available.
     */
    public <T> T executeWithLockAndRetry(String lockKey, Duration lockTtl,
                                          int maxRetries, long retryDelayMs,
                                          Supplier<T> task) {
        for (int i = 0; i < maxRetries; i++) {
            if (tryLock(lockKey, lockTtl)) {
                try {
                    return task.get();
                } finally {
                    unlock(lockKey);
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(retryDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Lock acquisition interrupted", e);
            }
        }
        throw new RuntimeException("Failed to acquire lock after " + maxRetries + " attempts");
    }
}
