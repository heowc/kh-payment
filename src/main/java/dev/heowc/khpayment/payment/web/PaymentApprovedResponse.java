package dev.heowc.khpayment.payment.web;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "from")
public class PaymentApprovedResponse {
    private String tid;
    private String corpResponse;
}
