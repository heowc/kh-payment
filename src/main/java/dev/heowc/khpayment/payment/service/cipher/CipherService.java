package dev.heowc.khpayment.payment.service.cipher;

public interface CipherService<E_R, D_R> {

    E_R decrypt(D_R param);
    D_R encrypt(E_R encryptedParam);
}
