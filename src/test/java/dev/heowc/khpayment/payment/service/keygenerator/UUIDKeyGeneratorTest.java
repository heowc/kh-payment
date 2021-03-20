package dev.heowc.khpayment.payment.service.keygenerator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UUIDKeyGeneratorTest {

    @DisplayName("테스트")
    @Test
    void test() {
        final KeyGenerator generator = new UUIDKeyGenerator();
        assertThat(generator.generate()).hasSizeLessThanOrEqualTo(20);
    }
}