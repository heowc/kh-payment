package dev.heowc.khpayment.paymentrequest.service;

import lombok.*;
import org.springframework.util.StringUtils;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRequestFrame {

    /* header fields */
    private String length;
    private String status;
    private String id;

    /* body fields */
    private String cardNumber;
    private String installMonth;
    private String expirationAt;
    private String cvc;
    private String amount;
    private String vatAmount;
    private String originTid;
    private String encryptedCardInfo;
    private String reserved;

    public static PaymentRequestFrame create(PaymentRequestEntity entity) {
        final PaymentRequestFrame frame = new PaymentRequestFrame();
        /* header (exclude `length`) */
        frame.status = FrameType.CHARACTER.toString(entity.getHeader().getStatus(), 10);
        frame.id = FrameType.CHARACTER.toString(entity.getHeader().getId(), 20);
        /* body */
        frame.cardNumber = FrameType.NUMBERIC_L.toString(entity.getBody().getCardNumber(), 20);
        frame.installMonth = FrameType.NUMBERIC_O.toString(entity.getBody().getInstallMonth(), 2);
        frame.expirationAt = FrameType.NUMBERIC_L.toString(entity.getBody().getExpirationAt(), 4);
        frame.cvc = FrameType.NUMBERIC_L.toString(entity.getBody().getCvc(), 3);
        frame.amount = FrameType.NUMBERIC.toString(String.valueOf(entity.getBody().getAmount().longValue()), 10);
        frame.vatAmount = FrameType.NUMBERIC_O.toString(String.valueOf(entity.getBody().getVatAmount().longValue()), 10);
        frame.originTid = FrameType.CHARACTER.toString(entity.getBody().getOriginTid() == null ?
                                                        "" : entity.getBody().getOriginTid(), 20);
        frame.encryptedCardInfo = FrameType.CHARACTER.toString(entity.getBody().getEncryptedCardInfo(), 300);
        frame.reserved = FrameType.CHARACTER.toString("", 47);

        frame.length = FrameType.NUMBERIC.toString(totalLength(frame), 4);
        return frame;
    }

    private static String totalLength(PaymentRequestFrame frame) {
        return String.valueOf(
                frame.status.length() +
                        frame.id.length() +
                        frame.cardNumber.length() +
                        frame.installMonth.length() +
                        frame.expirationAt.length() +
                        frame.cvc.length() +
                        frame.amount.length() +
                        frame.vatAmount.length() +
                        frame.originTid.length() +
                        frame.encryptedCardInfo.length() +
                        frame.reserved.length()
        );
    }

    public String toRaw() {
        return new StringBuilder()
                .append(length).append(status).append(id)
                .append(cardNumber).append(installMonth).append(expirationAt).append(cvc)
                .append(amount).append(vatAmount)
                .append(originTid)
                .append(encryptedCardInfo)
                .append(reserved)
                .toString();
    }

    @Getter
    @AllArgsConstructor(staticName = "from")
    private static class FrameElement {
        private final String value;
        private final int length;
    }

    private enum FrameType {
        NUMBERIC(element -> {
            validate(element);
            final String value = element.getValue();
            if (StringUtils.hasText(value)) {
                return fillCharacter(" ", element.getLength() - value.length()) + value;
            } else {
                return fillCharacter(" ", element.getLength());
            }
        }),
        NUMBERIC_O(element -> {
            validate(element);
            final String value = element.getValue();
            if (StringUtils.hasText(value)) {
                return fillCharacter("0", element.getLength() - value.length()) + value;
            } else {
                return fillCharacter("0", element.getLength());
            }
        }),
        NUMBERIC_L(element -> {
            validate(element);
            final String value = element.getValue();
            if (StringUtils.hasText(value)) {
                return value + fillCharacter(" ", element.getLength() - value.length());
            } else {
                return fillCharacter(" ", element.getLength());
            }
        }),
        CHARACTER(element -> {
            validate(element);
            final String value = element.getValue();
            if (StringUtils.hasText(value)) {
                return value + fillCharacter(" ", element.getLength() - value.length());
            } else {
                return fillCharacter(" ", element.getLength());
            }
        });

        private static void validate(FrameElement element) {
            if (element.getValue() == null) {
                throw new IllegalArgumentException("값이 비었습니다.");
            }
            if (element.getLength() < element.getValue().length()) {
                throw new IllegalArgumentException(String.format("프레임 길이를 초과했습니다. 최대길이=%d, 실제길이=%d",
                        element.getLength(), element.getValue().length()));
            }
        }

        private final Function<FrameElement, String> toString;

        FrameType(Function<FrameElement, String> toString) {
            this.toString = toString;
        }

        public String toString(String value, int length) {
            return toString.apply(FrameElement.from(value, length));
        }
    }

    static String fillCharacter(String word, int length) {
        return IntStream.range(0, length).mapToObj(it -> word).collect(Collectors.joining());
    }
}
