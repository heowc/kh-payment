package dev.heowc.khpayment.payment.repository;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(indexes = {
        @Index(name = "idx_payment_history_tid", columnList = "tid"),
        @Index(name = "idx_payment_history_aid", columnList = "aid", unique = true)
})
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String aid;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "install_month")
    private Integer installMonth;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "vat_amount")
    private BigDecimal vatAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @ManyToOne
    @JoinColumn(name = "tid", referencedColumnName = "tid",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Payment payment;

    @Builder
    public PaymentHistory(String aid, Status status, Integer installMonth, BigDecimal amount, BigDecimal vatAmount, Payment payment) {
        this.aid = requireNonNull(aid, "aid");
        this.status = requireNonNull(status, "status");
        this.amount = requireNonNull(amount, "amount");
        this.installMonth = requireNonNull(installMonth, "installMonth");
        this.vatAmount = requireNonNull(vatAmount, "vatAmount");
        this.payment = requireNonNull(payment, "payment");
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

    public enum Status {
        PAYMENT,
        CANCEL
    }
}
