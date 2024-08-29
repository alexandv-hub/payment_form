package com.example.payment_form.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentProcessingException.class)
    public ModelAndView handlePaymentProcessingException(PaymentProcessingException ex) {
        ModelAndView modelAndView = new ModelAndView("payment/payment_error");
        modelAndView.addObject("errorMessage", ex.getMessage());
        return modelAndView;
    }
}
