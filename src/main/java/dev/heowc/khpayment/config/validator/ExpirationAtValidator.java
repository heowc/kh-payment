package dev.heowc.khpayment.config.validator;

import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ExpirationAtValidator implements ConstraintValidator<ExpirationAt, String> {

    private DateTimeFormatter formatter;
    private YearMonth toYearMonth;

    @Override
    public void initialize(ExpirationAt constraintAnnotation) {
        this.formatter = DateTimeFormatter.ofPattern(constraintAnnotation.format());
        this.toYearMonth = YearMonth.parse(constraintAnnotation.to(), formatter);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.hasText(value)) {
            try {
                final YearMonth parsedYearMonth = YearMonth.parse(value, formatter);
                return parsedYearMonth.isAfter(YearMonth.now()) && parsedYearMonth.isBefore(toYearMonth);
            } catch (DateTimeParseException e) {
                return false;
            }

        } else {
            return false;
        }
    }
}