package dev.heowc.khpayment.payment.service.cipher;

import dev.heowc.khpayment.payment.service.CardInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultCardInfoCipherServiceTest {

    @DisplayName("실패")
    @Test
    void failure() {
        final CipherService<CardInfo, String> cipherService = new DefaultCardInfoCipherService();
        assertThatThrownBy(() -> {
            cipherService.decrypt(null);
        }).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> {
            cipherService.encrypt(null);
        }).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> {
            cipherService.decrypt("");
        }).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> {
            cipherService.decrypt("{}");
        }).isInstanceOf(RuntimeException.class);
    }

    @DisplayName("성공")
    @Test
    void successful() {
        final CipherService<CardInfo, String> cipherService = new DefaultCardInfoCipherService();
        final CardInfo expected = CardInfo.from("1234567890123456", "1122", "123");

        final String encrypted = cipherService.encrypt(expected);
        final CardInfo actual = cipherService.decrypt(encrypted);
        assertThat(actual).satisfies(it -> {
            assertThat(it.getCardNumber()).isEqualTo(expected.getCardNumber());
            assertThat(it.getExpirationAt()).isEqualTo(expected.getExpirationAt());
            assertThat(it.getCvc()).isEqualTo(expected.getCvc());
        });
    }
}