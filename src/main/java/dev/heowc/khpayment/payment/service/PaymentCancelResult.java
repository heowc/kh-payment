package dev.heowc.khpayment.payment.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "from")
public class PaymentCancelResult {

    private final String aid;
    private final String corpResponse;
}
