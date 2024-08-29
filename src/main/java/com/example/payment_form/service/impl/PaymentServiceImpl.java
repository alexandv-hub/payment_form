package com.example.payment_form.service.impl;

import com.example.payment_form.exception.PaymentProcessingException;
import com.example.payment_form.model.*;
import com.example.payment_form.repository.CustomerRepository;
import com.example.payment_form.repository.PaymentRequestRepository;
import com.example.payment_form.repository.PaymentResponseRepository;
import com.example.payment_form.service.HttpClientService;
import com.example.payment_form.service.JsonParserService;
import com.example.payment_form.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.http.HttpResponse;

import static com.example.payment_form.messages.ErrorMessages.PaymentServiceImpl.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${api.customer.referenceId}")
    private String referenceId;

    private final HttpClientService httpClientService;
    private final CustomerRepository customerRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final PaymentResponseRepository paymentResponseRepository;
    private final JsonParserService jsonParserService;
    private final ObjectMapper objectMapper;

    @Override
    public String processPayment(Payment payment) {
        log.info("Processing payment: {}",payment);

        payment = preparePayment(payment);

        var requestBody = prepareRequestBody(payment);
        log.info("Request body: {}",requestBody);
        var paymentRequest = savePaymentRequest(payment, requestBody);

        var responseOptional = httpClientService.sendPaymentRequest(requestBody);

        return responseOptional.map(response -> {
            savePaymentResponse(response, paymentRequest);
            return jsonParserService.extractRedirectUrl(response.body());
        }).orElseGet(() -> {
            log.error("Failed to get a response from the payment service.");
            return ERR_PAYMENT_FAILED_DUE_TO_NO_RESPONSE_FROM_THE_PAYMENT_SERVICE;
        });
    }

    private Payment preparePayment(Payment payment) {
        var customer = findOrCreateCustomer(referenceId);

        return payment.toBuilder()
                .paymentMethod(PaymentMethod.BASIC_CARD)
                .paymentType(PaymentType.DEPOSIT)
                .customer(customer)
                .build();
    }

    private Customer findOrCreateCustomer(String referenceId) {
        return customerRepository.findByReferenceId(referenceId)
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .referenceId(referenceId)
                        .build()));
    }

    private String prepareRequestBody(Payment payment) {
        try {
            return objectMapper.writeValueAsString(payment);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(ERR_FAILED_TO_SERIALIZE_PAYMENT_OBJECT, e);
        }
    }

    private PaymentRequest savePaymentRequest(Payment payment, String requestBody) {
        try {
            var paymentRequest = PaymentRequest.builder()
                    .payment(payment)
                    .requestBody(requestBody)
                    .build();

            return paymentRequestRepository.save(paymentRequest);
        } catch (DataAccessException e) {
            log.error("Error saving PaymentRequest: {}", e.getMessage());
            throw new PaymentProcessingException(ERR_FAILED_TO_SAVE_PAYMENT_REQUEST, e);
        }
    }

    private void savePaymentResponse(HttpResponse<String> response, PaymentRequest paymentRequest) {
        try {
            var responseId = jsonParserService.extractResponseId(response.body());
            log.info("Parsed responseId: {}", responseId);

            var paymentResponse = PaymentResponse.builder()
                    .responseBody(response.body())
                    .statusCode(response.statusCode())
                    .responseId(responseId)
                    .paymentRequest(paymentRequest)
                    .build();

            paymentResponseRepository.save(paymentResponse);
        } catch (DataAccessException e) {
            log.error("Error saving PaymentResponse: {}", e.getMessage());
            throw new PaymentProcessingException(ERR_FAILED_TO_SAVE_PAYMENT_RESPONSE, e);
        }
    }
}
