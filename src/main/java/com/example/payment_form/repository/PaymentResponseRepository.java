package com.example.payment_form.repository;

import com.example.payment_form.model.PaymentResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentResponseRepository extends JpaRepository<PaymentResponse, Long> {
}
