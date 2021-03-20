package dev.heowc.khpayment.common.service;

import dev.heowc.khpayment.TestUtil;
import dev.heowc.khpayment.common.CannotAcquireLockException;
import dev.heowc.khpayment.config.RedisServerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.SocketUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({SpringExtension.class})
@Import(value = {RedisServerConfig.class, RedisAutoConfiguration.class, RedisDistributionLockService.class})
class RedisDistributionLockServiceIT {

    @Autowired
    private DistributionLockService distributionLockService;

    @DynamicPropertySource
    static void registerEmbeddedRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.port", () -> String.valueOf(SocketUtils.findAvailableTcpPort()));
    }

    @DisplayName("동일한 LockType, 동일한 post으로 서로 다른 쓰레드에서 접근시, counter = 1")
    @Test
    void scenario1() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        final Runnable run1 = () -> {
            try {
                distributionLockService.tryLock(LockType.PAYMENT_APPROVED, "scenario1", () -> {
                    TestUtil.sleep(200L);
                    return "";
                });
            } catch (CannotAcquireLockException e) {
                counter.incrementAndGet();
            }
        };
        final Runnable run2 = () -> {
            try {
                distributionLockService.tryLock(LockType.PAYMENT_APPROVED, "scenario1", () -> {
                    return "";
                });
            } catch (CannotAcquireLockException e) {
                counter.incrementAndGet();
            }
        };

        assertCount(counter, run1, run2, 1);
    }

    @DisplayName("동일한 LockType, 서로 다른 post으로 서로 다른 쓰레드에서 접근시, counter = 0")
    @Test
    void scenario2() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        final Runnable run1 = () -> {
            try {
                distributionLockService.tryLock(LockType.PAYMENT_APPROVED, "scenario2_1", () -> {
                    TestUtil.sleep(200L);
                    return "";
                });
            } catch (CannotAcquireLockException e) {
                counter.incrementAndGet();
            }
        };
        final Runnable run2 = () -> {
            try {
                distributionLockService.tryLock(LockType.PAYMENT_APPROVED, "scenario2_2", () -> {
                    return "";
                });
            } catch (CannotAcquireLockException e) {
                counter.incrementAndGet();
            }
        };

        assertCount(counter, run1, run2, 0);
    }

    @DisplayName("서로 다른 LockType, 동일한 post으로 서로 다른 쓰레드에서 접근시, counter = 0")
    @Test
    void scenario3() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        final Runnable run1 = () -> {
            try {
                distributionLockService.tryLock(LockType.PAYMENT_CANCEL, "scenario3", () -> {
                    TestUtil.sleep(200L);
                    return "";
                });
            } catch (CannotAcquireLockException e) {
                counter.incrementAndGet();
            }
        };
        final Runnable run2 = () -> {
            try {
                distributionLockService.tryLock(LockType.PAYMENT_APPROVED, "scenario3", () -> {
                    return "";
                });
            } catch (CannotAcquireLockException e) {
                counter.incrementAndGet();
            }
        };

        assertCount(counter, run1, run2, 0);
    }

    @DisplayName("서로 다른 LockType, 서로 다른 post으로 서로 다른 쓰레드에서 접근시, counter = 0")
    @Test
    void scenario4() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        final Runnable run1 = () -> {
            try {
                distributionLockService.tryLock(LockType.PAYMENT_CANCEL, "scenario4_1", () -> {
                    TestUtil.sleep(200L);
                    return "";
                });
            } catch (CannotAcquireLockException e) {
                counter.incrementAndGet();
            }
        };
        final Runnable run2 = () -> {
            try {
                distributionLockService.tryLock(LockType.PAYMENT_APPROVED, "scenario4_2", () -> {
                    return "";
                });
            } catch (CannotAcquireLockException e) {
                counter.incrementAndGet();
            }
        };

        assertCount(counter, run1, run2, 0);
    }

    private static void assertCount(AtomicInteger counter, Runnable run1, Runnable run2, int i) throws InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture.allOf(
                    CompletableFuture.runAsync(run1, executorService),
                    CompletableFuture.runAsync(run2, executorService)).get();
            assertThat(counter.get()).isEqualTo(i);
        } catch (ExecutionException e) {
            // ignored
        }
    }
}
