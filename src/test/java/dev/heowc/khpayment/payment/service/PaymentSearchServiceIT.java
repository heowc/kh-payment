package dev.heowc.khpayment.payment.service;

import dev.heowc.khpayment.payment.repository.Payment;
import dev.heowc.khpayment.payment.repository.PaymentHistory;
import dev.heowc.khpayment.payment.repository.PaymentHistoryRepository;
import dev.heowc.khpayment.payment.repository.PaymentRepository;
import dev.heowc.khpayment.payment.web.PaymentApprovedRequest;
import dev.heowc.khpayment.payment.web.PaymentCancelRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.SocketUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
class PaymentSearchServiceIT {

    @Autowired
    private PaymentSearchService service;
    @Autowired
    private PaymentApprovedService approvedService;
    @Autowired
    private PaymentCancelService cancelService;
    @Autowired
    private PaymentRepository repository;
    @Autowired
    private PaymentHistoryRepository historyRepository;

    @BeforeEach
    void setup() {
        repository.deleteAll();
        historyRepository.deleteAll();
    }

    @DynamicPropertySource
    static void registerEmbeddedRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.port", () -> String.valueOf(SocketUtils.findAvailableTcpPort()));
    }

    @DisplayName("결제 조회 - 없는 tid")
    @Test
    void test1() {
        final PaymentSearchResult result = service.search("not_found_tid");
        assertThat(result).isNotNull();
        assertThat(result.getPayment()).isNull();
        assertThat(result.getCardInfo()).isNull();
        assertThat(result.getPaymentHistories()).isNull();
    }

    @DisplayName("결제 조회 - 결제 승인만 함")
    @Test
    void test2() {
        final PaymentApprovedResult approvedResult = approvedService.approve(new PaymentApprovedRequest(
                "1234567890123456",
                "1123",
                "123",
                0,
                BigDecimal.valueOf(1000),
                null
        ));
        final PaymentSearchResult result = service.search(approvedResult.getTid());
        assertThat(result).isNotNull();
        assertThat(result.getPayment()).satisfies(it -> {
            assertThat(it.getTid()).isEqualTo(approvedResult.getTid());
            assertThat(it.getStatus()).isEqualTo(Payment.Status.PAYMENT);
        });
        assertThat(result.getCardInfo()).satisfies(it -> {
            assertThat(it.getCardNumber()).isEqualTo("1234567890123456");
            assertThat(it.getExpirationAt()).isEqualTo("1123");
            assertThat(it.getCvc()).isEqualTo("123");
        });
        assertThat(result.getPaymentHistories()).size().isEqualTo(1);
        assertThat(result.getPaymentHistories()).element(0).satisfies(it -> {
            assertThat(it.getPayment().getTid()).isEqualTo(approvedResult.getTid());
            assertThat(it.getStatus()).isEqualTo(PaymentHistory.Status.PAYMENT);
            assertThat(it.getAmount().longValue()).isEqualTo(1000);
            assertThat(it.getVatAmount().longValue()).isEqualTo(91);
        });
    }

    @DisplayName("결제 조회 - 결제 승인/결제 취소 한번(부분 취소)")
    @Test
    void test3() {
        final PaymentApprovedResult approvedResult = approvedService.approve(new PaymentApprovedRequest(
                "1234567890123456",
                "1123",
                "123",
                0,
                BigDecimal.valueOf(1000),
                null
        ));
        final PaymentCancelResult cancelResult = cancelService.cancel(new PaymentCancelRequest(
                approvedResult.getTid(),
                BigDecimal.valueOf(500),
                null
        ));
        final PaymentSearchResult result = service.search(approvedResult.getTid());
        assertThat(result).isNotNull();
        assertThat(result.getPayment()).satisfies(it -> {
            assertThat(it.getTid()).isEqualTo(approvedResult.getTid());
            assertThat(it.getStatus()).isEqualTo(Payment.Status.PART_CANCEL_PAYMENT);
        });
        assertThat(result.getCardInfo()).satisfies(it -> {
            assertThat(it.getCardNumber()).isEqualTo("1234567890123456");
            assertThat(it.getExpirationAt()).isEqualTo("1123");
            assertThat(it.getCvc()).isEqualTo("123");
        });
        assertThat(result.getPaymentHistories()).size().isEqualTo(2);
        assertThat(result.getPaymentHistories()).element(0).satisfies(it -> {
            assertThat(it.getPayment().getTid()).isEqualTo(approvedResult.getTid());
            assertThat(it.getStatus()).isEqualTo(PaymentHistory.Status.PAYMENT);
            assertThat(it.getAmount().longValue()).isEqualTo(1000);
            assertThat(it.getVatAmount().longValue()).isEqualTo(91);
        });
        assertThat(result.getPaymentHistories()).element(1).satisfies(it -> {
            assertThat(it.getPayment().getTid()).isEqualTo(approvedResult.getTid());
            assertThat(it.getAid()).isEqualTo(cancelResult.getAid());
            assertThat(it.getStatus()).isEqualTo(PaymentHistory.Status.CANCEL);
            assertThat(it.getAmount().longValue()).isEqualTo(500);
            assertThat(it.getVatAmount().longValue()).isEqualTo(45);
        });
    }

    @DisplayName("결제 조회 - 결제 승인/결제 취소 한번(전체 취소)")
    @Test
    void test4() {
        final PaymentApprovedResult approvedResult = approvedService.approve(new PaymentApprovedRequest(
                "1234567890123456",
                "1123",
                "123",
                0,
                BigDecimal.valueOf(1000),
                null
        ));
        final PaymentCancelResult cancelResult = cancelService.cancel(new PaymentCancelRequest(
                approvedResult.getTid(),
                BigDecimal.valueOf(1000),
                null
        ));
        final PaymentSearchResult result = service.search(approvedResult.getTid());
        assertThat(result).isNotNull();
        assertThat(result.getPayment()).satisfies(it -> {
            assertThat(it.getTid()).isEqualTo(approvedResult.getTid());
            assertThat(it.getStatus()).isEqualTo(Payment.Status.CANCEL_PAYMENT);
        });
        assertThat(result.getCardInfo()).satisfies(it -> {
            assertThat(it.getCardNumber()).isEqualTo("1234567890123456");
            assertThat(it.getExpirationAt()).isEqualTo("1123");
            assertThat(it.getCvc()).isEqualTo("123");
        });
        assertThat(result.getPaymentHistories()).size().isEqualTo(2);
        assertThat(result.getPaymentHistories()).element(0).satisfies(it -> {
            assertThat(it.getPayment().getTid()).isEqualTo(approvedResult.getTid());
            assertThat(it.getStatus()).isEqualTo(PaymentHistory.Status.PAYMENT);
            assertThat(it.getAmount().longValue()).isEqualTo(1000);
            assertThat(it.getVatAmount().longValue()).isEqualTo(91);
        });
        assertThat(result.getPaymentHistories()).element(1).satisfies(it -> {
            assertThat(it.getPayment().getTid()).isEqualTo(approvedResult.getTid());
            assertThat(it.getAid()).isEqualTo(cancelResult.getAid());
            assertThat(it.getStatus()).isEqualTo(PaymentHistory.Status.CANCEL);
            assertThat(it.getAmount().longValue()).isEqualTo(1000);
            assertThat(it.getVatAmount().longValue()).isEqualTo(91);
        });
    }
}