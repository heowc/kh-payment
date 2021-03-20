package dev.heowc.khpayment.payment.util;

import dev.heowc.khpayment.payment.service.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class VatAmountUtilTest {

    @DisplayName("테스트")
    @ParameterizedTest
    @MethodSource("provideParamArguments")
    void test(BigDecimal param, BigDecimal expected) {
        final BigDecimal actual = VatAmountUtil.autoCalculate(param);
        assertThat(actual).isEqualTo(expected);
    }

    public static Stream<? extends Arguments> provideParamArguments() {
        return Stream.of(
                Arguments.of(null, BigDecimal.ZERO),
                Arguments.of(BigDecimal.valueOf(100), BigDecimal.valueOf(9)),
                Arguments.of(BigDecimal.valueOf(500), BigDecimal.valueOf(45)),
                Arguments.of(BigDecimal.valueOf(1_000), BigDecimal.valueOf(91)),
                Arguments.of(BigDecimal.valueOf(5_000), BigDecimal.valueOf(455)),
                Arguments.of(BigDecimal.valueOf(10_000), BigDecimal.valueOf(909)),
                Arguments.of(BigDecimal.valueOf(15_000), BigDecimal.valueOf(1_364)),
                Arguments.of(BigDecimal.valueOf(20_000), BigDecimal.valueOf(1_818)),
                Arguments.of(BigDecimal.valueOf(1_000_000_000), BigDecimal.valueOf(90_909_091))
        );
    }
}