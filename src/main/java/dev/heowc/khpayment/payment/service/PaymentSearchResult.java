package dev.heowc.khpayment.payment.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.heowc.khpayment.payment.repository.Payment;
import dev.heowc.khpayment.payment.repository.PaymentHistory;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentSearchResult {

    @JsonIgnore
    private static final PaymentSearchResult EMPTY = new PaymentSearchResult();

    private CardInfo cardInfo;
    private Payment payment;
    private List<PaymentHistory> paymentHistories;

    @Builder
    protected PaymentSearchResult(CardInfo cardInfo, Payment payment, List<PaymentHistory> paymentHistories) {
        this.cardInfo = requireNonNull(cardInfo, "cardInfo");
        this.payment = requireNonNull(payment, "payment");
        this.paymentHistories = requireNonNull(paymentHistories, "paymentHistories");
    }

    public static PaymentSearchResult empty() {
        return EMPTY;
    }
}
