package dev.heowc.khpayment.paymentrequest.service;

import dev.heowc.khpayment.payment.service.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static dev.heowc.khpayment.paymentrequest.service.PaymentRequestFrame.fillCharacter;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentRequestFrameTest {

    @DisplayName("결제 승인 요청 테스트")
    @Test
    void whenPaymentEntityThenRaw() {
        final PaymentRequestEntity entity = PaymentRequestEntity.builder()
                .header(PaymentRequestEntity.Header.builder()
                        .id("test_id")
                        .status(PaymentStatus.PAYMENT.name())
                        .build())
                .body(PaymentRequestEntity.Body.builder()
                        .cardNumber("1234567890123456")
                        .installMonth("0")
                        .expirationAt("1123")
                        .cvc("123")
                        .amount(BigDecimal.valueOf(1_000))
                        .vatAmount(BigDecimal.valueOf(91))
                        .originTid(null)
                        .encryptedCardInfo(fillCharacter("X", 250))
                        .build())
                .build();

        PaymentRequestFrame actual = PaymentRequestFrame.create(entity);
        assertThat(actual).isNotNull();
        assertThat(actual.getLength()).isEqualTo(" 446");
        assertThat(actual.getStatus()).isEqualTo("PAYMENT   ");
        assertThat(actual.getId()).isEqualTo("test_id             ");
        assertThat(actual.getCardNumber()).isEqualTo("1234567890123456    ");
        assertThat(actual.getInstallMonth()).isEqualTo("00");
        assertThat(actual.getExpirationAt()).isEqualTo("1123");
        assertThat(actual.getCvc()).isEqualTo("123");
        assertThat(actual.getAmount()).isEqualTo("      1000");
        assertThat(actual.getVatAmount()).isEqualTo("0000000091");
        assertThat(actual.getOriginTid()).isEqualTo("                    ");
        assertThat(actual.getEncryptedCardInfo()).isEqualTo(
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                "                                                  ");
        assertThat(actual.getReserved()).isEqualTo("                                               ");
        assertThat(actual.toRaw()).hasSize(450);
        assertThat(actual.toRaw()).isEqualTo(
                " 446" + /* 길이 */
                "PAYMENT   " + /* 구분 */
                "test_id             " + /* 관리번호 */
                "1234567890123456    " + /* 카드번호 */
                "00" + /* 할부개월 */
                "1123" + /* 유효날짜 */
                "123" + /* cvc */
                "      1000" + /* 결제/취소 금액 */
                "0000000091" + /* 결제/취소 부가가치세 */
                "                    " + /* 원래 관리번호 */
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                "                                                  " + /* 암호화된 카드 정보 */
                "                                               " /* 여유 필드 */
        );
    }

    @DisplayName("결제 취소 요청 테스트")
    @Test
    void whenCancelEntityThenRaw() {
        final PaymentRequestEntity entity = PaymentRequestEntity.builder()
                .header(PaymentRequestEntity.Header.builder()
                        .id("test_id")
                        .status(PaymentStatus.CANCEL.name())
                        .build())
                .body(PaymentRequestEntity.Body.builder()
                        .cardNumber("1234567890123456")
                        .installMonth("0")
                        .expirationAt("1123")
                        .cvc("123")
                        .amount(BigDecimal.valueOf(1_000))
                        .vatAmount(BigDecimal.valueOf(91))
                        .originTid("origin_tid")
                        .encryptedCardInfo(fillCharacter("X", 250))
                        .build())
                .build();

        PaymentRequestFrame actual = PaymentRequestFrame.create(entity);
        assertThat(actual).isNotNull();
        assertThat(actual.getLength()).isEqualTo(" 446");
        assertThat(actual.getStatus()).isEqualTo("CANCEL    ");
        assertThat(actual.getId()).isEqualTo("test_id             ");
        assertThat(actual.getCardNumber()).isEqualTo("1234567890123456    ");
        assertThat(actual.getInstallMonth()).isEqualTo("00");
        assertThat(actual.getExpirationAt()).isEqualTo("1123");
        assertThat(actual.getCvc()).isEqualTo("123");
        assertThat(actual.getAmount()).isEqualTo("      1000");
        assertThat(actual.getVatAmount()).isEqualTo("0000000091");
        assertThat(actual.getOriginTid()).isEqualTo("origin_tid          ");
        assertThat(actual.getEncryptedCardInfo()).isEqualTo(
                        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                        "                                                  ");
        assertThat(actual.getReserved()).isEqualTo("                                               ");
        assertThat(actual.toRaw()).hasSize(450);
        assertThat(actual.toRaw()).isEqualTo(
                        " 446" + /* 길이 */
                        "CANCEL    " + /* 구분 */
                        "test_id             " + /* 관리번호 */
                        "1234567890123456    " + /* 카드번호 */
                        "00" + /* 할부개월 */
                        "1123" + /* 유효날짜 */
                        "123" + /* cvc */
                        "      1000" + /* 결제/취소 금액 */
                        "0000000091" + /* 결제/취소 부가가치세 */
                        "origin_tid          " + /* 원래 관리번호 */
                        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                        "                                                  " + /* 암호화된 카드 정보 */
                        "                                               " /* 여유 필드 */
        );
    }

    @DisplayName("테스트")
    @ParameterizedTest
    @MethodSource("provideEntityParamArguments")
    void test(String headerId, String headerStatus, String bodyCardNumber, String bodyInstallMonth,
              String bodyExpirationAt, String bodyCvc, BigDecimal bodyAmount, BigDecimal bodyVatAmount,
              String bodyOriginTid, String bodyEncryptedCardInfo, Boolean isNull) {
        final PaymentRequestEntity entity = PaymentRequestEntity.builder()
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

        PaymentRequestFrame frame = null;

        try {
            frame = PaymentRequestFrame.create(entity);
        } catch (IllegalArgumentException e) {
            // ignored
        }
        assertThat(frame == null).isEqualTo(isNull);
    }

    public static Stream<? extends Arguments> provideEntityParamArguments() {
        return Stream.of(
                // 성공
                Arguments.of("test_id", PaymentStatus.PAYMENT.name(), "1234567890123456", "0", "1221", "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, "XXXXXXXXXXXXXXXXXXXX", false)
                // 실패
                , Arguments.of("123456789012345678901", PaymentStatus.PAYMENT.name(), "1234567890123456", "0", "1221", "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, fillCharacter("X", 300), true)
                , Arguments.of("test_id", "12345678901", "1234567890123456", "0", "1221", "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, fillCharacter("X", 300), true)
                , Arguments.of("test_id", PaymentStatus.PAYMENT.name(), "123456789012345678901", "0", "1221", "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, fillCharacter("X", 300), true)
                , Arguments.of("test_id", PaymentStatus.PAYMENT.name(), "1234567890123456", "123", "1221", "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, fillCharacter("X", 300), true)
                , Arguments.of("test_id", PaymentStatus.PAYMENT.name(), "1234567890123456", "0", "12345", "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, fillCharacter("X", 300), true)
                , Arguments.of("test_id", PaymentStatus.PAYMENT.name(), "1234567890123456", "0", "1221", "1234",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, fillCharacter("X", 300), true)
                , Arguments.of("test_id", PaymentStatus.PAYMENT.name(), "1234567890123456", "0", "1221", "333",
                        BigDecimal.valueOf(10_000_000_000L), BigDecimal.valueOf(100), null, fillCharacter("X", 300), true)
                , Arguments.of("test_id", PaymentStatus.PAYMENT.name(), "1234567890123456", "0", "1221", "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(10_000_000_000L), null, fillCharacter("X", 300), true)
                , Arguments.of("test_id", PaymentStatus.PAYMENT.name(), "1234567890123456", "0", "1221", "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), "123456789012345678901", fillCharacter("X", 300), true)
                , Arguments.of("test_id", PaymentStatus.PAYMENT.name(), "1234567890123456", "0", "1221", "333",
                        BigDecimal.valueOf(10_000), BigDecimal.valueOf(100), null, fillCharacter("X", 301), true)

        );
    }
}