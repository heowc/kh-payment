package dev.heowc.khpayment.payment.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class VatAmountUtil {

    private static final BigDecimal VAT_RATE = BigDecimal.valueOf(11);

    public static BigDecimal autoCalculate(BigDecimal paymentAmount) {
        if (paymentAmount == null) {
            return BigDecimal.ZERO;
        }
        return paymentAmount.divide(VAT_RATE, RoundingMode.HALF_UP);
    }

    private VatAmountUtil() { }
}
