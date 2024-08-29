package com.example.payment_form.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Constraint(validatedBy = AmountValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface ValidAmount {
    String message() default "Invalid amount format. Amount must be in the following formats: 1234 or 123.4 or 12.34";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
