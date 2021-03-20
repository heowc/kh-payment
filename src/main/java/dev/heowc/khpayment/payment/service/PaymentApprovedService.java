package dev.heowc.khpayment.payment.service;

import dev.heowc.khpayment.common.service.DistributionLockService;
import dev.heowc.khpayment.common.service.LockType;
import dev.heowc.khpayment.payment.repository.Payment;
import dev.heowc.khpayment.payment.repository.PaymentHistory;
import dev.heowc.khpayment.payment.repository.PaymentHistoryRepository;
import dev.heowc.khpayment.payment.repository.PaymentRepository;
import dev.heowc.khpayment.payment.service.cipher.CipherService;
import dev.heowc.khpayment.payment.service.keygenerator.KeyGenerator;
import dev.heowc.khpayment.payment.util.VatAmountUtil;
import dev.heowc.khpayment.payment.web.PaymentApprovedRequest;
import dev.heowc.khpayment.paymentrequest.service.PaymentRequestEntity;
import dev.heowc.khpayment.paymentrequest.service.PaymentRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Slf4j
@Service
public class PaymentApprovedService {

    private final DistributionLockService distributionLockService;
    private final KeyGenerator keyGenerator;
    private final CipherService<CardInfo, String> cipherService;
    private final PaymentRequestService paymentRequestService;
    private final PaymentRepository repository;
    private final PaymentHistoryRepository historyRepository;

    public PaymentApprovedService(DistributionLockService distributionLockService,
                                  KeyGenerator keyGenerator,
                                  CipherService<CardInfo, String> cipherService,
                                  PaymentRequestService paymentRequestService,
                                  PaymentRepository repository,
                                  PaymentHistoryRepository historyRepository) {
        this.distributionLockService = distributionLockService;
        this.keyGenerator = keyGenerator;
        this.cipherService = cipherService;
        this.paymentRequestService = paymentRequestService;
        this.repository = repository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public PaymentApprovedResult approve(PaymentApprovedRequest request) {
        final BigDecimal vatAmount = request.getVatAmount();
        if (vatAmount != null) {
            if (request.getPaymentAmount().compareTo(vatAmount) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "부가가치세가 결제금액보다 크거나 같을 수 없습니다.");
            }
        }
        return distributionLockService.tryLock(LockType.PAYMENT_APPROVED, request.getCardNumber(), () -> {
            final CardInfo cardInfo =
                    CardInfo.from(request.getCardNumber(), request.getExpirationAt(), request.getCvc());
            final String encryptedCardInfo = encryptCardInfo(cardInfo);

            final Payment payment = newPayment(request, encryptedCardInfo);

            // 카드사 통신
            final String corpResponse =
                    paymentRequestService.execute(toPaymentRequest(payment, cardInfo, encryptedCardInfo));

            // 결제 내역 저장 (payment, payment_history)
            final Payment createdPayment = repository.save(payment);
            final PaymentHistory paymentHistory = newPaymentHistory(createdPayment);
            historyRepository.save(paymentHistory);
            // 응답
            return PaymentApprovedResult.from(createdPayment.getTid(), corpResponse);
        });
    }

    private String encryptCardInfo(CardInfo cardInfo) {
        return cipherService.encrypt(cardInfo);
    }

    private PaymentRequestEntity toPaymentRequest(Payment payment, CardInfo cardInfo, String encryptedCardInfo) {
        return PaymentRequestEntity.builder()
                .header(PaymentRequestEntity.Header.builder()
                        .id(payment.getTid())
                        .status(PaymentStatus.PAYMENT.name())
                        .build())
                .body(PaymentRequestEntity.Body.builder()
                        .cardNumber(cardInfo.getCardNumber())
                        .installMonth(String.valueOf(payment.getInstallMonth()))
                        .expirationAt(cardInfo.getExpirationAt())
                        .cvc(cardInfo.getCvc())
                        .amount(payment.getPaymentAmount())
                        .vatAmount(payment.getPaymentAmount())
                        .encryptedCardInfo(encryptedCardInfo)
                        .build())
                .build();
    }

    private Payment newPayment(PaymentApprovedRequest request, String encryptedCardInfo) {
        final Payment.PaymentBuilder builder = Payment.builder();
        if (request.getVatAmount() == null) {
            builder.vatAmount(VatAmountUtil.autoCalculate(request.getPaymentAmount()));
        } else {
            builder.vatAmount(request.getVatAmount());
        }
        return builder
                .tid(keyGenerator.generate())
                .status(Payment.Status.PAYMENT)
                .encryptedCardInfo(encryptedCardInfo)
                .installMonth(request.getInstallMonth())
                .paymentAmount(request.getPaymentAmount())
                .canceledAmount(BigDecimal.ZERO)
                .canceledVatAmount(BigDecimal.ZERO)
                .build();
    }

    private PaymentHistory newPaymentHistory(Payment payment) {
        return PaymentHistory.builder()
                .aid(keyGenerator.generate())
                .status(PaymentHistory.Status.PAYMENT)
                .installMonth(payment.getInstallMonth())
                .amount(payment.getPaymentAmount())
                .vatAmount(payment.getVatAmount())
                .payment(payment)
                .build();
    }
}
