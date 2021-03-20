package dev.heowc.khpayment.payment.service.cipher;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.heowc.khpayment.payment.service.CardInfo;
import dev.heowc.khpayment.util.JsonUtil;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
class DefaultCardInfoCipherService implements CipherService<CardInfo, String> {

    @Override
    public CardInfo decrypt(String param) {
        requireNonNull(param, "param");
        final String decodedParam = new String(Base64Utils.decodeFromString(param), StandardCharsets.UTF_8);
        log.info("card info - decryption: {}", decodedParam);
        final InternalCardInfo internalCardInfo = JsonUtil.convertStringToObject(decodedParam, InternalCardInfo.class);
        return internalCardInfo.toCarInfo();
    }

    @Override
    public String encrypt(CardInfo cardInfo) {
        requireNonNull(cardInfo, "cardInfo");
        final InternalCardInfo internalCardInfo = InternalCardInfo.of(cardInfo);
        final String json = JsonUtil.convertObjectToString(internalCardInfo);
        final String encode = Base64Utils.encodeToString(json.getBytes(StandardCharsets.UTF_8));
        log.info("card info - encryption: {}", encode);
        return encode;
    }

    @Data
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class InternalCardInfo {
        private String cardNumber;
        private String expirationAt;
        private String cvc;

        private InternalCardInfo(String cardNumber, String expirationAt, String cvc) {
            this.cardNumber = cardNumber;
            this.expirationAt = expirationAt;
            this.cvc = cvc;
        }

        @JsonIgnore
        public static InternalCardInfo of(CardInfo cardInfo) {
            return new InternalCardInfo(cardInfo.getCardNumber(), cardInfo.getExpirationAt(), cardInfo.getCvc());
        }

        @JsonIgnore
        public CardInfo toCarInfo() {
            return CardInfo.from(cardNumber, expirationAt, cvc);
        }
    }
}
