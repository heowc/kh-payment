package dev.heowc.khpayment.payment.web;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PaymentCancelRequest {

    @NotBlank(message = "트랜잭션 ID가 비었습니다.")
    private String tid;

    @NotNull(message = "취소금액이 비었습니다.")
    @Positive(message = "취소금액이 0보다 커야합니다.")
    private BigDecimal cancelAmount;

    @PositiveOrZero(message = "부가가치체가 0이거나 0보다 커야합니다.")
    private BigDecimal cancelVatAmount;
}
