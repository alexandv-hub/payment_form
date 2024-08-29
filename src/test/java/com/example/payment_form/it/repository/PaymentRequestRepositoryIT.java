package com.example.payment_form.it.repository;


import com.example.payment_form.model.*;
import com.example.payment_form.repository.PaymentRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
public class PaymentRequestRepositoryIT {

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
            "postgres:latest")
            .withReuse(Boolean.FALSE);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
    }

    @AfterAll
    static void afterAll() {
        postgreSQLContainer.stop();
    }

    @Test
    public void testSaveAndRetrievePaymentRequest() {
        var customer = Customer.builder()
                .referenceId("customer_1")
                .build();

        var payment = Payment.builder()
                .paymentType(PaymentType.DEPOSIT)
                .paymentMethod(PaymentMethod.BASIC_CARD)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .customer(customer)
                .build();

        var paymentRequest = PaymentRequest.builder()
                .payment(payment)
                .requestBody("{\"example\":\"data\"}")
                .build();
        paymentRequest = paymentRequestRepository.save(paymentRequest);

        var foundRequestOptional = paymentRequestRepository.findById(paymentRequest.getId());
        assertTrue(foundRequestOptional.isPresent());
        assertEquals(paymentRequest.getRequestBody(), foundRequestOptional.get().getRequestBody());
    }
}
