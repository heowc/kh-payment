package dev.heowc.khpayment.common.service;

import dev.heowc.khpayment.common.CannotAcquireLockException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.function.Supplier;

@Service
class RedisDistributionLockService implements DistributionLockService {

    private static final Duration DEFAULT_TIMEOUT = Duration.of(3, ChronoUnit.SECONDS);
    private final StringRedisTemplate redisTemplate;

    public RedisDistributionLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public <T> T tryLock(LockType lockType, String postfix, Duration timeout, Supplier<T> supplier) {
        final String lockKey = lockType.lockKey(postfix);
        final Boolean tryLock = redisTemplate.opsForValue().setIfAbsent(lockKey, lockKey, DEFAULT_TIMEOUT);
        if (Boolean.TRUE.equals(tryLock)) {
            try {
                return supplier.get();
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            throw new CannotAcquireLockException();
        }
    }

    @Override
    public <T> T tryLock(LockType lockType, String postfix, Supplier<T> supplier) {
        return tryLock(lockType, postfix, DEFAULT_TIMEOUT, supplier);
    }


    @Override
    public <T> T tryLock(LockType lockType, String postfix, long l, TemporalUnit unit, Supplier<T> supplier) {
        return tryLock(lockType, postfix, Duration.of(l, unit), supplier);
    }
}
