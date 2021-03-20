package dev.heowc.khpayment.paymentrequest.service;

import dev.heowc.khpayment.payment.service.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentRequestEntityTest {

    @DisplayName("테스트")
    @ParameterizedTest
    @MethodSource("provideEntityParamArguments")
    void test(String headerId, String headerStatus, String bodyCardNumber, String bodyInstallMonth,
              String bodyExpirationAt, String bodyCvc, BigDecimal bodyAmount, BigDecimal bodyVatAmount,
              String bodyOriginTid, String bodyEncryptedCardInfo, Boolean isNull) {
        PaymentRequestEntity entity = null;
        try {
            entity = PaymentRequestEntity.builder()
                    .header(PaymentRequestEntity.Header.builder()
                            .id(headerId)
                            .status(headerStatus)
                            .build())
                    .body(PaymentRequestEntity.Body.builder()
                            .cardNumber(bodyCardNumber)
                            .installMonth(bodyInstallMonth)
                            .expirationAt(bodyExpirationAt)
                            .cvc(bodyCvc)
                            .amount(bodyAmount)
                            .vatAmount(bodyVatAmount)
                            .originTid(bodyOriginTid)
                            .encryptedCardInfo(bodyEncryptedCardInfo)
                            .build())
                    .build();
        } catch (NullPointerException e) {
            // ignored
        }
        assertThat(entity == null).isEqualTo(isNull);
    }

    public static Stream<? extends Arguments> provideEntityParamArguments() {
        return Stream.of(
                // 성공
                Arguments.of("test_aid", PaymentStatus.PAYMENT.name(), "1234567890123456", "0", "1221", "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, "XXXXXXXXXXXXXXXXXXXX", false)
                // 실패
                , Arguments.of(null, PaymentStatus.PAYMENT.name(), "1234567890123456", "0", "1221", "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, "XXXXXXXXXXXXXXXXXXXX", true)
                , Arguments.of("test_aid", null, "1234567890123456", "0", "1221", "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, "XXXXXXXXXXXXXXXXXXXX", true)
                , Arguments.of("test_aid", PaymentStatus.PAYMENT.name(), null, "0", "1221", "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, "XXXXXXXXXXXXXXXXXXXX", true)
                , Arguments.of("test_aid", PaymentStatus.PAYMENT.name(), "1234567890123456", null, "1221", "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, "XXXXXXXXXXXXXXXXXXXX", true)
                , Arguments.of("test_aid", PaymentStatus.PAYMENT.name(), "1234567890123456", "0", null, "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, "XXXXXXXXXXXXXXXXXXXX", true)
                , Arguments.of("test_aid", PaymentStatus.PAYMENT.name(), "1234567890123456", "0", "1221", null,
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, "XXXXXXXXXXXXXXXXXXXX", true)
                , Arguments.of("test_aid", PaymentStatus.PAYMENT.name(), "1234567890123456", "0", "1221", "333",
                        null, BigDecimal.valueOf(100), null, "XXXXXXXXXXXXXXXXXXXX", true)
                , Arguments.of("test_aid", PaymentStatus.PAYMENT.name(), "1234567890123456", "0", "1221", "333",
                        BigDecimal.valueOf(10_000), null, null, "XXXXXXXXXXXXXXXXXXXX", true)
                , Arguments.of("test_aid", PaymentStatus.PAYMENT.name(), "1234567890123456", "0", "1221", "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, null, true)
        );
    }
}