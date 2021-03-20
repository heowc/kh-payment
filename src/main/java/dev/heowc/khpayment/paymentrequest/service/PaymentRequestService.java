package dev.heowc.khpayment.paymentrequest.service;

import dev.heowc.khpayment.paymentrequest.repository.PaymentRequestRaw;
import dev.heowc.khpayment.paymentrequest.repository.PaymentRequestRawRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentRequestService {

    private final PaymentRequestRawRepository repository;

    public PaymentRequestService(PaymentRequestRawRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String execute(PaymentRequestEntity request) {
        final String raw = generateRaw(request);
        repository.save(PaymentRequestRaw.of(raw));
        return raw;
    }

    private static String generateRaw(PaymentRequestEntity entity) {
        final PaymentRequestFrame frame = PaymentRequestFrame.create(entity);
        return frame.toRaw();
    }
}
