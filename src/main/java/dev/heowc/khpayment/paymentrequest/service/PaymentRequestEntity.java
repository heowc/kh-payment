package dev.heowc.khpayment.paymentrequest.service;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

@Getter
@ToString
@Builder(builderClassName = "PaymentRequestEntityBuilder", buildMethodName = "build")
public class PaymentRequestEntity {

    private final Header header;
    private final Body body;

    public static class PaymentRequestEntityBuilder {
        public PaymentRequestEntity build() {
            requireNonNull(header, "header");
            requireNonNull(body, "body");
            return new PaymentRequestEntity(header, body);
        }
    }

    @Getter
    @Builder(builderClassName = "HeaderBuilder", buildMethodName = "build")
    public static class Header {
        private final String id;
        private final String status;

        public static class HeaderBuilder {
            public Header build() {
                requireNonNull(id, "id");
                requireNonNull(status, "status");
                return new Header(id, status);
            }
        }
    }

    @Getter
    @Builder(builderClassName = "BodyBuilder", buildMethodName = "build")
    public static class Body {
        private final String cardNumber;
        private final String installMonth;
        private final String expirationAt;
        private final String cvc;
        private final BigDecimal amount;
        private final BigDecimal vatAmount;
        private final String originTid;
        private final String encryptedCardInfo;

        public static class BodyBuilder {
            public Body build() {
                requireNonNull(cardNumber, "id");
                requireNonNull(installMonth, "status");
                requireNonNull(expirationAt, "expirationAt");
                requireNonNull(cvc, "cvc");
                requireNonNull(amount, "amount");
                requireNonNull(vatAmount, "vatAmount");
                requireNonNull(encryptedCardInfo, "encryptedCardInfo");
                return new Body(cardNumber, installMonth, expirationAt, cvc, amount, vatAmount,
                        originTid, encryptedCardInfo);
            }
        }
    }
}
