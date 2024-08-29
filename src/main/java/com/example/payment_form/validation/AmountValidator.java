package com.example.payment_form.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class AmountValidator implements ConstraintValidator<ValidAmount, String> {

    @Value("${validation.amount.pattern}")
    private String pattern;

    @Override
    public void initialize(ValidAmount constraintAnnotation) {
        log.info("Initialized with pattern: {}", pattern);
    }

    @Override
    public boolean isValid(String amount, ConstraintValidatorContext context) {
        log.info("Validating amount {}", amount);

        try {
            if (amount == null) {
                log.error("Amount is null.");
                return false;
            }

            boolean isValid = amount.matches(pattern);

            if (!isValid) {
                log.error("Invalid amount format: '{}'. Expected format is 12.34", amount);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Unexpected error during amount validation: {}", e.getMessage());
            return false;
        }
    }
}