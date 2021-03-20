package dev.heowc.khpayment.payment.service;

import dev.heowc.khpayment.payment.repository.Payment;
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
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@DirtiesContext
class PaymentCancelServiceIT {

    @Autowired
    private PaymentApprovedService approvedService;
    @Autowired
    private PaymentCancelService service;
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

    @DisplayName("취소 실패 - 없는 결제 내역")
    @Test
    void test() {
        final PaymentCancelRequest request = new PaymentCancelRequest(
                "not_found_tid",
                null,
                null
        );
        assertThatThrownBy(() -> {
            service.cancel(request);
        }).isInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"결제내역이 없습니다.\"");
    }

    @DisplayName("취소 성공 - 부가가치세 자동 계산")
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
        final PaymentCancelRequest request = new PaymentCancelRequest(
                approvedResult.getTid(),
                BigDecimal.valueOf(500),
                null
        );
        final PaymentCancelResult result = service.cancel(request);
        assertThat(result).isNotNull();
        assertThat(result.getAid()).hasSize(20);
        assertThat(result.getCorpResponse()).hasSize(450);
        final Payment payment = repository.findByTid(approvedResult.getTid()).get();
        assertThat(payment.getCanceledAmount().longValue()).isEqualTo(500L);
        assertThat(payment.getCanceledVatAmount().longValue()).isEqualTo(45L);
    }

    @DisplayName("취소 성공 - 부가가치세 지정")
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
        final PaymentCancelRequest request = new PaymentCancelRequest(
                approvedResult.getTid(),
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(10)
        );
        final PaymentCancelResult result = service.cancel(request);
        assertThat(result).isNotNull();
        final Payment payment = repository.findByTid(approvedResult.getTid()).get();
        assertThat(payment.getCanceledAmount().longValue()).isEqualTo(500L);
        assertThat(payment.getCanceledVatAmount().longValue()).isEqualTo(10L);
    }

    @DisplayName("취소 실패 - 취소 결제금액이 더 큼")
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
        final PaymentCancelRequest request = new PaymentCancelRequest(
                approvedResult.getTid(),
                BigDecimal.valueOf(1001),
                null
        );
        assertThatThrownBy(() -> {
            service.cancel(request);
        }).isInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"취소금액이 남은 결제금액보다 클 수 없습니다.\"");
    }

    @DisplayName("취소 실패 - 취소 결제금액이 더 큼")
    @Test
    void test5() {
        final PaymentApprovedResult approvedResult = approvedService.approve(new PaymentApprovedRequest(
                "1234567890123456",
                "1123",
                "123",
                0,
                BigDecimal.valueOf(1000),
                null
        ));
        final PaymentCancelRequest request = new PaymentCancelRequest(
                approvedResult.getTid(),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(92)
        );
        assertThatThrownBy(() -> {
            service.cancel(request);
        }).isInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"취소 부가가치세가 남은 부가가치세보다 클 수 없습니다.\"");
    }

    @DisplayName("취소 실패 - 취소 결제금액이 더 큼")
    @Test
    void test6() {
        final PaymentApprovedResult approvedResult = approvedService.approve(new PaymentApprovedRequest(
                "1234567890123456",
                "1123",
                "123",
                0,
                BigDecimal.valueOf(1000),
                null
        ));
        final PaymentCancelRequest request = new PaymentCancelRequest(
                approvedResult.getTid(),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(90)
        );
        assertThatThrownBy(() -> {
            service.cancel(request);
        }).isInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"남은 결제금액은 없지만 남은 부가가치세가 있습니다.\"");
    }
}