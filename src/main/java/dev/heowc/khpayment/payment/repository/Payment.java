package dev.heowc.khpayment.payment.repository;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(indexes = {
        @Index(name = "idx_payment_tid", unique = true, columnList = "tid")
})
public class Payment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @Column(length = 20)
    private String tid;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(length = 300, name = "encrypted_card_info")
    private String encryptedCardInfo;

    @Column(name = "payment_amount")
    private BigDecimal paymentAmount;

    @Column(name = "vat_amount")
    private BigDecimal vatAmount;

    @Column(name = "install_month")
    private Integer installMonth;

    @Column(name = "canceled_amount")
    private BigDecimal canceledAmount;

    @Column(name = "canceled_vat_amount")
    private BigDecimal canceledVatAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @OneToMany(mappedBy = "payment")
    private List<PaymentHistory> paymentHistories = new ArrayList<>();

    @Builder(builderClassName = "PaymentBuilder", buildMethodName = "build")
    private Payment(String tid, Status status, String encryptedCardInfo, Integer installMonth,
                    BigDecimal paymentAmount, BigDecimal vatAmount,
                    BigDecimal canceledAmount, BigDecimal canceledVatAmount) {
        this.tid = tid;
        this.status = status;
        this.encryptedCardInfo = encryptedCardInfo;
        this.installMonth = installMonth;
        this.paymentAmount = paymentAmount;
        this.vatAmount = vatAmount;
        this.canceledAmount = canceledAmount;
        this.canceledVatAmount = canceledVatAmount;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

    public void applyCancel(BigDecimal canceledAmount, BigDecimal canceledVatAmount) {
        plusCanceledAmount(canceledAmount);
        plusCanceledVatAmount(canceledVatAmount);
        changeStatus();
    }

    private void plusCanceledAmount(BigDecimal canceledAmount) {
        this.canceledAmount = this.canceledAmount.add(canceledAmount);
    }

    private void plusCanceledVatAmount(BigDecimal canceledVatAmount) {
        this.canceledVatAmount = this.canceledVatAmount.add(canceledVatAmount);
        this.modifiedAt = LocalDateTime.now();
    }

    private void changeStatus() {
        if (this.canceledAmount.compareTo(BigDecimal.ZERO) == 0
                && this.canceledVatAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.status = Status.PAYMENT;
        } else if (this.paymentAmount.compareTo(this.canceledAmount) == 0
                && this.canceledVatAmount.compareTo(this.vatAmount) == 0) {
            this.status = Status.CANCEL_PAYMENT;
        } else {
            this.status = Status.PART_CANCEL_PAYMENT;
        }
        this.modifiedAt = LocalDateTime.now();
    }

    public enum Status {
        PAYMENT,
        PART_CANCEL_PAYMENT,
        CANCEL_PAYMENT
    }
}
