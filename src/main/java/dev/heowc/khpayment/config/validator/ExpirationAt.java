package dev.heowc.khpayment.config.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = ExpirationAtValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface ExpirationAt {

    String format();

    /**
     * format과 동일한 형식으로 지정만료일까지 검증
     */
    String to();

    String message() default "{javax.validation.constraints.ExpirationAt.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}