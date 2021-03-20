package dev.heowc.khpayment.payment.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.heowc.khpayment.payment.service.PaymentSearchResult;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class PaymentSearchResponse {

    @JsonIgnore
    private static final PaymentSearchResponse EMPTY = PaymentSearchResponse.builder().build();

    private final String tid;
    private final CardInfo cardInfo;
    private final String status;
    private final PaymentAmount amount;
    private final LocalDateTime approvedAt;
    private final List<PaymentActionDetail> paymentActionDetails;

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    public static class CardInfo {
        private final String cardNumber;
        private final String expirationAt;
        private final String cvc;
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    public static class PaymentAmount {
        private final Long paymentAmount;
        private final Long vatAmount;
        private final Long canceledAmount;
        private final Long canceledVatAmount;
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    public static class PaymentActionDetail {
        private final String aid;
        private final String type;
        private final LocalDateTime createdAt;
        private final Long amount;
        private final Long vat;
    }

    @JsonIgnore
    public static PaymentSearchResponse of(PaymentSearchResult result) {
        if (isNull(result)) {
            return EMPTY;
        }

        final PaymentSearchResponseBuilder builder = PaymentSearchResponse.builder();
        builder.tid(result.getPayment().getTid());
        builder.cardInfo(CardInfo.builder()
                .cardNumber(result.getCardInfo().getCardNumber())
                .expirationAt(result.getCardInfo().getExpirationAt())
                .cvc(result.getCardInfo().getCvc())
                .build());
        builder.status(result.getPayment().getStatus().name());
        builder.amount(PaymentAmount.builder()
                .paymentAmount(result.getPayment().getPaymentAmount().longValue())
                .vatAmount(result.getPayment().getVatAmount().longValue())
                .canceledAmount(result.getPayment().getCanceledAmount().longValue())
                .canceledVatAmount(result.getPayment().getCanceledVatAmount().longValue())
                .build());
        builder.approvedAt(result.getPayment().getCreatedAt());
        builder.paymentActionDetails(result.getPaymentHistories().stream()
                .map(it -> PaymentActionDetail.builder()
                        .aid(it.getAid())
                        .type(it.getStatus().name())
                        .amount(it.getAmount().longValue())
                        .vat(it.getVatAmount().longValue())
                        .createdAt(it.getCreatedAt())
                        .build()).collect(toImmutableList()));
        return builder.build();
    }

    @JsonIgnore
    private static boolean isNull(PaymentSearchResult result) {
        if (result == null) {
            return true;
        }
        if (result.getPayment() == null) {
            return true;
        }
        if (result.getCardInfo() == null) {
            return true;
        }
        return CollectionUtils.isEmpty(result.getPaymentHistories());
    }
}
