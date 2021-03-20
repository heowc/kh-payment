package dev.heowc.khpayment.payment.service;

import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "from")
public class CardInfo {

    private String cardNumber;
    private String expirationAt;
    private String cvc;
}
