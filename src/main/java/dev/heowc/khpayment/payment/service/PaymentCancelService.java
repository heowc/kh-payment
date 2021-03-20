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
import dev.heowc.khpayment.payment.web.PaymentCancelRequest;
import dev.heowc.khpayment.paymentrequest.service.PaymentRequestEntity;
import dev.heowc.khpayment.paymentrequest.service.PaymentRequestService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class PaymentCancelService {

    private static final int INSTALL_MONTH = 0;

    private final DistributionLockService distributionLockService;
    private final KeyGenerator keyGenerator;
    private final PaymentRepository repository;
    private final CipherService<CardInfo, String> cipherService;
    private final PaymentRequestService requestService;
    private final PaymentHistoryRepository historyRepository;

    public PaymentCancelService(DistributionLockService distributionLockService,
                                KeyGenerator keyGenerator,
                                PaymentRepository repository,
                                CipherService<CardInfo, String> cipherService,
                                PaymentRequestService requestService,
                                PaymentHistoryRepository historyRepository) {
        this.distributionLockService = distributionLockService;
        this.keyGenerator = keyGenerator;
        this.repository = repository;
        this.cipherService = cipherService;
        this.requestService = requestService;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public PaymentCancelResult cancel(PaymentCancelRequest request) {
        final Payment payment = repository.findByTid(request.getTid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "결제내역이 없습니다."));

        final CardInfo decryptedCardInfo = cipherService.decrypt(payment.getEncryptedCardInfo());

        return distributionLockService.tryLock(LockType.PAYMENT_CANCEL, decryptedCardInfo.getCardNumber(), () -> {

            final ValidatedCancelAmount validatedCancelAmount = validateCancelAmount(request, payment);
            payment.applyCancel(validatedCancelAmount.getCancelAmount(),
                                validatedCancelAmount.getCancelVatAmount());

            final PaymentHistory paymentHistory = newPaymentHistory(payment, validatedCancelAmount);
            final String corpResponse =
                    requestService.execute(toPaymentRequest(payment, paymentHistory, decryptedCardInfo));

            repository.save(payment);
            historyRepository.save(paymentHistory);

            return PaymentCancelResult.from(paymentHistory.getAid(), corpResponse);
        });
    }

    private PaymentHistory newPaymentHistory(Payment existedPayment, ValidatedCancelAmount validatedCancelAmount) {
        return PaymentHistory.builder()
                .aid(keyGenerator.generate())
                .status(PaymentHistory.Status.CANCEL)
                .installMonth(INSTALL_MONTH)
                .amount(validatedCancelAmount.getCancelAmount())
                .vatAmount(validatedCancelAmount.getCancelVatAmount())
                .payment(existedPayment)
                .build();
    }

    private static ValidatedCancelAmount validateCancelAmount(PaymentCancelRequest request, Payment payment) {
        final BigDecimal paymentAmount = payment.getPaymentAmount();
        final BigDecimal remainedPaymentAmount = paymentAmount.subtract(payment.getCanceledAmount());
        if (remainedPaymentAmount.compareTo(request.getCancelAmount()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "취소금액이 남은 결제금액보다 클 수 없습니다.");
        }

        final BigDecimal actualCancelVatAmount = extractCancelVatAmount(request, payment);
        final BigDecimal remainedVatAmount = payment.getVatAmount().subtract(payment.getCanceledVatAmount());
        if (remainedVatAmount.compareTo(actualCancelVatAmount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "취소 부가가치세가 남은 부가가치세보다 클 수 없습니다.");
        }

        if (remainedPaymentAmount.subtract(request.getCancelAmount()).compareTo(BigDecimal.ZERO) == 0 &&
                remainedVatAmount.subtract(actualCancelVatAmount).compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "남은 결제금액은 없지만 남은 부가가치세가 있습니다.");
        }

        return ValidatedCancelAmount.form(request.getCancelAmount(), actualCancelVatAmount);
    }

    private static BigDecimal extractCancelVatAmount(PaymentCancelRequest request, Payment payment) {
        BigDecimal actualCancelVatAmount = request.getCancelVatAmount();
        if (actualCancelVatAmount != null) {
            return actualCancelVatAmount;
        }

        final BigDecimal autoCalculatedCancelVatAmount = VatAmountUtil.autoCalculate(request.getCancelAmount());
        final BigDecimal remainedVatAmount = payment.getVatAmount().subtract(payment.getCanceledVatAmount());
        if (autoCalculatedCancelVatAmount.compareTo(remainedVatAmount) > 0) {
            actualCancelVatAmount = remainedVatAmount;
        } else {
            actualCancelVatAmount = autoCalculatedCancelVatAmount;
        }
        return actualCancelVatAmount;
    }

    private PaymentRequestEntity toPaymentRequest(Payment payment, PaymentHistory paymentHistory, CardInfo cardInfo) {
        return PaymentRequestEntity.builder()
                .header(PaymentRequestEntity.Header.builder()
                        .id(paymentHistory.getAid())
                        .status(PaymentStatus.CANCEL.name())
                        .build())
                .body(PaymentRequestEntity.Body.builder()
                        .cardNumber(cardInfo.getCardNumber())
                        .installMonth(String.valueOf(INSTALL_MONTH))
                        .expirationAt(cardInfo.getExpirationAt())
                        .cvc(cardInfo.getCvc())
                        .amount(paymentHistory.getAmount())
                        .vatAmount(paymentHistory.getVatAmount())
                        .originTid(payment.getTid())
                        .encryptedCardInfo(payment.getEncryptedCardInfo())
                        .build())
                .build();
    }

    @Getter
    @ToString
    @AllArgsConstructor(staticName = "form")
    private static class ValidatedCancelAmount {
        private final BigDecimal cancelAmount;
        private final BigDecimal cancelVatAmount;
    }
}
