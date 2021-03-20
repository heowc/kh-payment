package dev.heowc.khpayment.paymentrequest.repository;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PaymentRequestRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String raw;

    public static PaymentRequestRaw of(String raw) {
        final PaymentRequestRaw requestRaw = new PaymentRequestRaw();
        requestRaw.raw = raw;
        return requestRaw;
    }
}
