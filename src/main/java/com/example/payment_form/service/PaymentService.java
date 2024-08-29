package com.example.payment_form.service;

import com.example.payment_form.model.Payment;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {

    String processPayment(Payment payment);

}
