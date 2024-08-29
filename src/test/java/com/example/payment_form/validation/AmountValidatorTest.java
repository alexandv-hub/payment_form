package com.example.payment_form.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AmountValidatorTest {

    public static final String VALIDATION_AMOUNT_PATTERN = "^\\d{1,12}(\\.\\d{1,2})?$";

    @InjectMocks
    private AmountValidator amountValidator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(amountValidator, "pattern", VALIDATION_AMOUNT_PATTERN);
    }

    @Test
    void testValidAmount() {
        assertTrue(amountValidator.isValid("10.00", context));
        assertTrue(amountValidator.isValid("123456789012.99", context));
        assertTrue(amountValidator.isValid("0.01", context));
    }

    @Test
    void testInvalidAmount() {
        assertFalse(amountValidator.isValid("10.0a", context));
        assertFalse(amountValidator.isValid("123.123", context));
        assertFalse(amountValidator.isValid("1234567890123.99", context));
    }
}
