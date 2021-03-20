package dev.heowc.khpayment.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "중복 요청이 발생했습니다.")
public class CannotAcquireLockException extends RuntimeException {

    public CannotAcquireLockException(String message) {
        super(message);
    }

    public CannotAcquireLockException() {
        super((String)null);
    }
}
