package dev.heowc.khpayment.payment.web;

import dev.heowc.khpayment.config.validator.ExpirationAt;
import lombok.*;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PaymentApprovedRequest {

    @NotBlank(message = "카드번호가 비었습니다.")
    @Size(min = 10, max = 20, message = "카드번호의 길이가 10~20자 사이어야 합니다.")
    private String cardNumber;

    @NotBlank(message = "카드 유효날짜가 비었습니다.")
    @ExpirationAt(format = "MMyy", to = "0125", message = "카드 유효날짜가 올바르지 않습니다.")
    private String expirationAt;

    @NotBlank(message = "CVC번호가 비었습니다.")
    @Size(min = 3, max = 3, message = "CVC번호의 길이가 올바르지 않습니다. (3자)")
    private String cvc;

    @NotNull(message = "카드 할부개월 수가 비었습니다.")
    @Range(min = 0, max = 12, message = "카드 할부개월 수는 0(일시불), 1~12 사이어야 합니다.")
    private Integer installMonth;

    @NotNull(message = "결제금액이 비었습니다.")
    @Range(min = 100, max = 1_000_000_000, message = "결제금액이 100원이상 10억 사이어야 합니다.")
    private BigDecimal paymentAmount;

    @PositiveOrZero(message = "부가가치체가 0이거나 0보다 커야합니다.")
    private BigDecimal vatAmount;
}
