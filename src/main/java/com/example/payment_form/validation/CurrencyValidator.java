package com.example.payment_form.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Currency;

@Slf4j
public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    @Override
    public boolean isValid(String currencyCode, ConstraintValidatorContext context) {
        log.info("Checking if currency code {} is valid", currencyCode);

        try {
            Currency.getInstance(currencyCode);
            return true;
        } catch (IllegalArgumentException e) {
            log.error("Invalid currency code: '{}'. This currency code is not recognized.", currencyCode);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during currency validation for code '{}': {}", currencyCode, e.getMessage());
            return false;
        }
    }
}
