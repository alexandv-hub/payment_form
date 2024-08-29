package com.example.payment_form.controller;

import com.example.payment_form.dto.PaymentRequestDTO;
import com.example.payment_form.mapper.PaymentMapper;
import com.example.payment_form.model.Payment;
import com.example.payment_form.service.CurrencyService;
import com.example.payment_form.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentFormControllerV1 {

    private final CurrencyService currencyService;
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @GetMapping("/form")
    public String showPaymentForm(Model model) {
        model.addAttribute("payment", new Payment());
        model.addAttribute("currencies", currencyService.getCurrencies());
        return "payment/payment_form";
    }

    @PostMapping("/process")
    public String processPayment(Model model, @Valid @ModelAttribute("payment") PaymentRequestDTO paymentRequestDTO, BindingResult bindingResult) {
        log.info("Processing paymentRequestDTO {}", paymentRequestDTO);

        if (bindingResult.hasErrors()) {
            log.error("Validation errors found: {}", bindingResult.getAllErrors());
            return "payment/payment_error";
        }

        var payment = paymentMapper.mapToPayment(paymentRequestDTO);
        var redirectUrl = paymentService.processPayment(payment);
        log.info("redirectUrl = {}", redirectUrl);

        return "redirect:" + redirectUrl;
    }
}
