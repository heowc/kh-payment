package dev.heowc.khpayment.payment.service;

import dev.heowc.khpayment.payment.repository.Payment;
import dev.heowc.khpayment.payment.repository.PaymentHistoryRepository;
import dev.heowc.khpayment.payment.repository.PaymentRepository;
import dev.heowc.khpayment.payment.web.PaymentApprovedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.SocketUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext
class PaymentApprovedServiceIT {

    @Autowired
    private PaymentApprovedService service;
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

    @DisplayName("결제 성공 - 부가가치세 자동계산됨")
    @Test
    void test1() {
        final PaymentApprovedRequest request = new PaymentApprovedRequest(
                "1234567890123456",
                "1123",
                "123",
                0,
                BigDecimal.valueOf(1000),
                null
        );
        final PaymentApprovedResult approvedResult = service.approve(request);
        assertThat(approvedResult).isNotNull();
        assertThat(approvedResult.getTid()).hasSize(20);
        assertThat(approvedResult.getCorpResponse()).hasSize(450);
        final Payment payment = repository.findByTid(approvedResult.getTid()).get();
        assertThat(approvedResult.getTid()).isEqualTo(payment.getTid());
        assertThat(payment.getPaymentAmount().longValue()).isEqualTo(1000L);
        assertThat(payment.getVatAmount().longValue()).isEqualTo(91L);
    }

    @DisplayName("결제 성공 - 부가가치세 지정")
    @Test
    void test2() {
        final PaymentApprovedRequest request = new PaymentApprovedRequest(
                "1234567890123456",
                "1123",
                "123",
                0,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(0)
        );
        final PaymentApprovedResult approvedResult = service.approve(request);
        assertThat(approvedResult).isNotNull();
        assertThat(approvedResult.getTid()).hasSize(20);
        assertThat(approvedResult.getCorpResponse()).hasSize(450);
        final Payment payment = repository.findByTid(approvedResult.getTid()).get();
        assertThat(approvedResult.getTid()).isEqualTo(payment.getTid());
        assertThat(payment.getPaymentAmount().longValue()).isEqualTo(1000L);
        assertThat(payment.getVatAmount().longValue()).isEqualTo(0L);
    }

    @DisplayName("결제 실패 - 부가가시체가 결제금액이랑 같음")
    @Test
    void test3() {
        assertThatThrownBy(() -> {
            final PaymentApprovedRequest request = new PaymentApprovedRequest(
                    "1234567890123456",
                    "1123",
                    "123",
                    0,
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(1000)
            );
            service.approve(request);
        }).isInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"부가가치세가 결제금액보다 크거나 같을 수 없습니다.\"");
    }

    @DisplayName("결제 실패 - 부가가시체가 결제금액보다 큼")
    @Test
    void test4() {
        assertThatThrownBy(() -> {
            final PaymentApprovedRequest request = new PaymentApprovedRequest(
                    "1234567890123456",
                    "1123",
                    "123",
                    0,
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(1001)
            );
            service.approve(request);
        }).isInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"부가가치세가 결제금액보다 크거나 같을 수 없습니다.\"");
    }
}