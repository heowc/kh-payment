package dev.heowc.khpayment.common.service;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.function.Supplier;

public interface DistributionLockService {

    <T> T tryLock(LockType lockType, String postfix, Duration timeout, Supplier<T> supplier);

    <T> T tryLock(LockType lockType, String postfix, Supplier<T> supplier);

    <T> T tryLock(LockType lockType, String postfix, long l, TemporalUnit unit, Supplier<T> supplier);
}
