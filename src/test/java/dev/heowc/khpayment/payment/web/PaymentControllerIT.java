package dev.heowc.khpayment.payment.web;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dev.heowc.khpayment.payment.repository.PaymentHistoryRepository;
import dev.heowc.khpayment.payment.repository.PaymentRepository;
import dev.heowc.khpayment.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class PaymentControllerIT {

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

    @DisplayName("결제 승인 요청 - 파라미터 체크")
    @ParameterizedTest
    @MethodSource("providePaymentApprovedRequestArguments")
    void testApprove(String cardNumber, String expirationAt, String cvc, Integer installMonth,
              BigDecimal amount, BigDecimal vatAmount, HttpStatus status, String message) {
        final PaymentApprovedRequest request = new PaymentApprovedRequest(
                cardNumber,
                expirationAt,
                cvc,
                installMonth,
                amount,
                vatAmount
        );
        final ResponseEntity<String> responseEntity =
                restTemplate.postForEntity("/v1/payment/approve", request, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(status);
        if (status != HttpStatus.OK) {
            final String body = responseEntity.getBody();
            final Map<String, Object> map =
                    (Map<String, Object>) JsonUtil.convertStringToObject(body, Map.class);
            assertThat(map.get("message")).isEqualTo(message);
        }
    }

    public static Stream<? extends Arguments> providePaymentApprovedRequestArguments() {
        return Stream.of(
                // 성공
                Arguments.of("1234567890123456", "1123", "123", 0, BigDecimal.valueOf(1_000), null, HttpStatus.OK, null)
                // 실패
                , Arguments.of(null, "1123", "123", 0, BigDecimal.valueOf(1_000), null,
                        HttpStatus.BAD_REQUEST, "카드번호가 비었습니다.")
                , Arguments.of("1234567890123456", null, "123", 0, BigDecimal.valueOf(1_000), null,
                        HttpStatus.BAD_REQUEST, "카드 유효날짜가 비었습니다.")
                , Arguments.of("1234567890123456", "1323", "123", 0, BigDecimal.valueOf(1_000), null,
                        HttpStatus.BAD_REQUEST, "카드 유효날짜가 올바르지 않습니다.")
                , Arguments.of("1234567890123456", "0101", "123", 0, BigDecimal.valueOf(1_000), null,
                        HttpStatus.BAD_REQUEST, "카드 유효날짜가 올바르지 않습니다.")
                , Arguments.of("1234567890123456", "0225", "123", 0, BigDecimal.valueOf(1_000), null,
                        HttpStatus.BAD_REQUEST, "카드 유효날짜가 올바르지 않습니다.")
                , Arguments.of("1234567890123456", "1123", null, 0, BigDecimal.valueOf(1_000), null,
                        HttpStatus.BAD_REQUEST, "CVC번호가 비었습니다.")
                , Arguments.of("1234567890123456", "1123", "123", -1, BigDecimal.valueOf(1_000), null,
                        HttpStatus.BAD_REQUEST, "카드 할부개월 수는 0(일시불), 1~12 사이어야 합니다.")
                , Arguments.of("1234567890123456", "1123", "123", 13, BigDecimal.valueOf(1_000), null,
                        HttpStatus.BAD_REQUEST, "카드 할부개월 수는 0(일시불), 1~12 사이어야 합니다.")
                , Arguments.of("1234567890123456", "1123", "123", 0, BigDecimal.valueOf(99), null,
                        HttpStatus.BAD_REQUEST, "결제금액이 100원이상 10억 사이어야 합니다.")
                , Arguments.of("1234567890123456", "1123", "123", 0, BigDecimal.valueOf(1_000_000_001L), null,
                        HttpStatus.BAD_REQUEST, "결제금액이 100원이상 10억 사이어야 합니다.")
                , Arguments.of("1234567890123456", "1123", "123", 0, BigDecimal.valueOf(1_000), BigDecimal.valueOf(-1),
                        HttpStatus.BAD_REQUEST, "부가가치체가 0이거나 0보다 커야합니다.")
                , Arguments.of("1234567890123456", "1123", "123", 0, BigDecimal.valueOf(1_000), BigDecimal.valueOf(1_000),
                        HttpStatus.BAD_REQUEST, "부가가치세가 결제금액보다 크거나 같을 수 없습니다.")
                , Arguments.of("1234567890123456", "1123", "123", 0, BigDecimal.valueOf(1_000), BigDecimal.valueOf(1_001),
                        HttpStatus.BAD_REQUEST, "부가가치세가 결제금액보다 크거나 같을 수 없습니다.")
        );
    }

    @DisplayName("결제 취소 요청 - 파라미터 체크")
    @ParameterizedTest
    @MethodSource("providePaymentCancelRequestArguments")
    void testCancel(String tid,BigDecimal cancelAmount, BigDecimal cancelVatAmount, HttpStatus status, String message) {
        final PaymentCancelRequest request = new PaymentCancelRequest(
                tid,
                cancelAmount,
                cancelVatAmount
        );
        final ResponseEntity<String> responseEntity =
                restTemplate.postForEntity("/v1/payment/cancel", request, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(status);
        if (status != HttpStatus.OK) {
            final String body = responseEntity.getBody();
            final Map<String, Object> map =
                    (Map<String, Object>) JsonUtil.convertStringToObject(body, Map.class);
            assertThat(map.get("message")).isEqualTo(message);
        }
    }

    public static Stream<? extends Arguments> providePaymentCancelRequestArguments() {
        return Stream.of(
                Arguments.of("not_found_tid", BigDecimal.valueOf(1_000), null,
                        HttpStatus.BAD_REQUEST, "결제내역이 없습니다.")
                , Arguments.of(null, BigDecimal.valueOf(1_000), null,
                        HttpStatus.BAD_REQUEST, "트랜잭션 ID가 비었습니다.")
                , Arguments.of("not_found_tid", null, null,
                        HttpStatus.BAD_REQUEST, "취소금액이 비었습니다.")
                , Arguments.of("not_found_tid", BigDecimal.valueOf(-1), null,
                        HttpStatus.BAD_REQUEST, "취소금액이 0보다 커야합니다.")
                , Arguments.of("not_found_tid", BigDecimal.valueOf(1_000), BigDecimal.valueOf(-1),
                        HttpStatus.BAD_REQUEST, "부가가치체가 0이거나 0보다 커야합니다.")
        );
    }

    @DisplayName("결제 조회 요청 - 파라미터 체크")
    @ParameterizedTest
    @MethodSource("providePaymentSearchParamArguments")
    void testSearch(String name, List<String> value, HttpStatus status, String message) {
        final UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl("http://localhost:" + port)
                .path("/v1/payment")
                .queryParam(name, value)
                .build();
        final ResponseEntity<String> responseEntity =
                restTemplate.getForEntity(uriComponents.toString(), String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(status);
        if (status != HttpStatus.OK) {
            final String body = responseEntity.getBody();
            final Map<String, Object> map =
                    (Map<String, Object>) JsonUtil.convertStringToObject(body, Map.class);
            assertThat(map.get("message")).isEqualTo(message);
        }
    }

    public static Stream<? extends Arguments> providePaymentSearchParamArguments() {
        return Stream.of(
                Arguments.of("tid", ImmutableList.of("not_found"), HttpStatus.OK, null)
                , Arguments.of("tid", ImmutableList.of(""), HttpStatus.BAD_REQUEST, "트랜잭션 id가 비었습니다.")
                , Arguments.of("", ImmutableList.of(""), HttpStatus.BAD_REQUEST, "트랜잭션 id가 비었습니다.")
        );
    }
}