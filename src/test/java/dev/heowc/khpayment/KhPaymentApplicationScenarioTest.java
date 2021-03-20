package dev.heowc.khpayment;

import dev.heowc.khpayment.payment.repository.PaymentHistoryRepository;
import dev.heowc.khpayment.payment.repository.PaymentRepository;
import dev.heowc.khpayment.payment.web.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.SocketUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class KhPaymentApplicationScenarioTest {

    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

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

    /**
     * 결제      / 11000 / 1000 / 성공 / 11000 / 1000
     * 부분 취소 / 1100  / 100  / 성공 / 9900  / 900
     * 부분 취소 / 3300  / null / 성공 / 6600  / 600
     * 부분 취소 / 7000  / null / 실패 / 6600  / 600
     * 부분 취소 / 6600  / 700  / 실패 / 6600  / 600
     * 부분 취소 / 6600  / 600  / 성공 / 0     / 0
     * 부분 취소 / 100   / null / 실패 / 0     / 0
     */
    @DisplayName("시나리오 1")
    @Test
    void scenario1() {
        final String tid =
                assertApproved(BigDecimal.valueOf(11_000), BigDecimal.valueOf(1_000), 11_000L, 1_000L);
        assertCancel(tid, BigDecimal.valueOf(1_100), BigDecimal.valueOf(100), HttpStatus.OK, 9900L, 900L);
        assertCancel(tid, BigDecimal.valueOf(3_300), null, HttpStatus.OK, 6600L, 600L);
        assertCancel(tid, BigDecimal.valueOf(7_000), null, HttpStatus.BAD_REQUEST, 6600L, 600L);
        assertCancel(tid, BigDecimal.valueOf(6_600), BigDecimal.valueOf(700), HttpStatus.BAD_REQUEST, 6600L, 600L);
        assertCancel(tid, BigDecimal.valueOf(6_600), BigDecimal.valueOf(600), HttpStatus.OK, 0L, 0L);
        assertCancel(tid, BigDecimal.valueOf(100), null, HttpStatus.BAD_REQUEST, 0L, 0L);
    }

    /**
     * 결제      / 20000 / 909  / 성공 / 20000 / 909
     * 부분 취소 / 10000  / 0   / 성공 / 10000 / 909
     * 부분 취소 / 10000  / 0   / 실패 / 10000 / 909
     * 부분 취소 / 10000  / 909 / 성공 / 0     / 0
     */
    @DisplayName("시나리오 2")
    @Test
    void scenario2() {
        final String tid =
                assertApproved(BigDecimal.valueOf(20_000), BigDecimal.valueOf(909), 20_000L, 909L);
        assertCancel(tid, BigDecimal.valueOf(10_000), BigDecimal.valueOf(0), HttpStatus.OK, 10_000L, 909L);
        assertCancel(tid, BigDecimal.valueOf(10_000), BigDecimal.valueOf(0), HttpStatus.BAD_REQUEST, 10_000L, 909L);
        assertCancel(tid, BigDecimal.valueOf(10_000), BigDecimal.valueOf(909), HttpStatus.OK, 0L, 0L);
    }

    /**
     * 결제      / 20000 / null / 성공 / 20000 / 1818
     * 부분 취소 / 10000 / 1000 / 성공 / 10000 / 818
     * 부분 취소 / 10000 / 909  / 실패 / 10000 / 818
     * 부분 취소 / 10000 / null / 성공 / 0     / 0
     */
    @DisplayName("시나리오 3")
    @Test
    void scenario3() {
        final String tid =
                assertApproved(BigDecimal.valueOf(20_000), null, 20_000L, 1_818L);
        assertCancel(tid, BigDecimal.valueOf(10_000), BigDecimal.valueOf(1_000), HttpStatus.OK, 10_000L, 818L);
        assertCancel(tid, BigDecimal.valueOf(10_000), BigDecimal.valueOf(909), HttpStatus.BAD_REQUEST, 10_000L, 818L);
        assertCancel(tid, BigDecimal.valueOf(10_000), null, HttpStatus.OK, 0L, 0L);
    }

    private String assertApproved(BigDecimal amount, BigDecimal vat,
                                  Long remainedAmount, Long remainedVatAmount) {
        final ResponseEntity<PaymentApprovedResponse> responseEntity =
                restTemplate.postForEntity("/v1/payment/approve", new PaymentApprovedRequest(
                        "1234567890123456",
                        "1123",
                        "123",
                        0,
                        amount,
                        vat
                ), PaymentApprovedResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        final String tid = responseEntity.getBody().getTid();
        assertSearch(tid, remainedAmount, remainedVatAmount);
        return tid;
    }

    private void assertCancel(String tid, BigDecimal amount, BigDecimal vat, HttpStatus status,
                              Long remainedAmount, Long remainedVatAmount) {
        final ResponseEntity<PaymentCancelResponse> responseEntity =
                restTemplate.postForEntity("/v1/payment/cancel", new PaymentCancelRequest(
                        tid,
                        amount,
                        vat
                ), PaymentCancelResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(status);

        assertSearch(tid, remainedAmount, remainedVatAmount);
    }

    private void assertSearch(String tid, Long remainedAmount, Long remainedVatAmount) {
        final UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl("http://localhost:" + port)
                .path("/v1/payment")
                .queryParam("tid", tid)
                .build();
        final ResponseEntity<PaymentSearchResponse> searchResponseEntity =
                restTemplate.getForEntity(uriComponents.toString(), PaymentSearchResponse.class);
        final Long paymentAmount = searchResponseEntity.getBody().getAmount().getPaymentAmount();
        final Long vatAmount = searchResponseEntity.getBody().getAmount().getVatAmount();
        final Long canceledAmount = searchResponseEntity.getBody().getAmount().getCanceledAmount();
        final Long canceledVatAmount = searchResponseEntity.getBody().getAmount().getCanceledVatAmount();
        assertThat(paymentAmount - canceledAmount).isEqualTo(remainedAmount);
        assertThat(vatAmount - canceledVatAmount).isEqualTo(remainedVatAmount);
    }
}
