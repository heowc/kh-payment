package dev.heowc.khpayment.payment.web;

import dev.heowc.khpayment.payment.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping("v1/payment")
public class PaymentController {

    private final PaymentApprovedService approvedService;
    private final PaymentCancelService cancelService;
    private final PaymentSearchService searchService;

    public PaymentController(PaymentApprovedService approvedService,
                             PaymentCancelService cancelService,
                             PaymentSearchService searchService) {
        this.approvedService = approvedService;
        this.cancelService = cancelService;
        this.searchService = searchService;
    }

    @PostMapping("/approve")
    public PaymentApprovedResponse approve(@Valid @RequestBody(required = false) PaymentApprovedRequest request,
                                           BindingResult result) {
        validateRequestBody(request, result);
        final PaymentApprovedResult approved = approvedService.approve(request);
        return PaymentApprovedResponse.from(approved.getTid(), approved.getCorpResponse());
    }

    @PostMapping("/cancel")
    public PaymentCancelResponse cancel(@Valid @RequestBody(required = false) PaymentCancelRequest request,
                                        BindingResult result) {
        validateRequestBody(request, result);
        final PaymentCancelResult canceled = cancelService.cancel(request);
        return PaymentCancelResponse.from(canceled.getAid(), canceled.getCorpResponse());
    }

    @GetMapping
    public PaymentSearchResponse search(@RequestParam(name = "tid", required = false) String tid) {
        if (!StringUtils.hasText(tid)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "트랜잭션 id가 비었습니다.");
        }
        return PaymentSearchResponse.of(searchService.search(tid));
    }

    private static void validateRequestBody(Object request, BindingResult result) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청이 비었습니다.");
        }
        if (result.hasErrors()) {
            final String message = result.getFieldError().getDefaultMessage();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
