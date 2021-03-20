package dev.heowc.khpayment.payment.service;

import com.google.common.collect.ImmutableList;
import dev.heowc.khpayment.payment.repository.Payment;
import dev.heowc.khpayment.payment.repository.PaymentHistory;
import dev.heowc.khpayment.payment.repository.PaymentRepository;
import dev.heowc.khpayment.payment.service.cipher.CipherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PaymentSearchService {

    private final PaymentRepository repository;
    private final CipherService<CardInfo, String> cipherService;

    public PaymentSearchService(PaymentRepository repository,
                                CipherService<CardInfo, String> cipherService) {
        this.repository = repository;
        this.cipherService = cipherService;
    }

    @Transactional(readOnly = true)
    public PaymentSearchResult search(String tid) {
        final Optional<Payment> paymentOp = repository.findByTid(tid);
        if (paymentOp.isPresent()) {
            final Payment payment = paymentOp.get();
            final CardInfo cardInfo = cipherService.decrypt(payment.getEncryptedCardInfo());
            final List<PaymentHistory> histories = payment.getPaymentHistories();
            return PaymentSearchResult.builder()
                    .payment(payment)
                    .cardInfo(cardInfo)
                    .paymentHistories(ImmutableList.copyOf(histories))
                    .build();
        } else {
            return PaymentSearchResult.empty();
        }

    }
}
