package dev.heowc.khpayment.common.service;

public enum LockType {
    PAYMENT_APPROVED("lock_payment_approved_"),
    PAYMENT_CANCEL("lock_payment_cancel_"),
    ;

    private final String prefix;

    LockType(String prefix) {
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }

    public String lockKey(String postfix) {
        return this.prefix() + postfix;
    }
}
