package com.example.payment_form.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CurrencyValidatorTest {

    @InjectMocks
    private CurrencyValidator currencyValidator;

    @Mock
    private ConstraintValidatorContext context;

    @Test
    void testValidCurrency() {
        assertTrue(currencyValidator.isValid("USD", context));
        assertTrue(currencyValidator.isValid("EUR", context));
        assertTrue(currencyValidator.isValid("JPY", context));
    }

    @Test
    void testInvalidCurrency() {
        assertFalse(currencyValidator.isValid("INVALID", context));
        assertFalse(currencyValidator.isValid("123", context));
        assertFalse(currencyValidator.isValid("$$$", context));
    }

    @Test
    void testNullCurrency() {
        assertFalse(currencyValidator.isValid(null, context));
    }
}
