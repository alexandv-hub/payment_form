package com.example.payment_form.service.impl;

import com.example.payment_form.exception.PaymentProcessingException;
import com.example.payment_form.model.*;
import com.example.payment_form.repository.CustomerRepository;
import com.example.payment_form.repository.PaymentRequestRepository;
import com.example.payment_form.repository.PaymentResponseRepository;
import com.example.payment_form.service.HttpClientService;
import com.example.payment_form.service.JsonParserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpResponse;
import java.util.Optional;

import static com.example.payment_form.messages.ErrorMessages.PaymentServiceImpl.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private HttpClientService httpClientService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PaymentRequestRepository paymentRequestRepository;

    @Mock
    private HttpResponse<String> httpResponse;

    @Mock
    private PaymentResponseRepository paymentResponseRepository;

    @Mock
    private JsonParserService jsonParserService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(
                httpClientService, customerRepository,
                paymentRequestRepository, paymentResponseRepository,
                jsonParserService, objectMapper);
    }

    @Test
    void givenValidPayment_whenProcessPayment_thenReturnRedirectUrl() throws Exception {
        var payment = new Payment();
        var paymentRequest = PaymentRequest.builder().build();

        when(httpClientService.sendPaymentRequest(anyString())).thenReturn(Optional.of(httpResponse));
        when(httpResponse.body()).thenReturn("{\"result\":{\"redirectUrl\":\"http://example.com\"}}");
        when(jsonParserService.extractRedirectUrl(httpResponse.body())).thenReturn("http://example.com");
        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenReturn(paymentRequest);
        when(objectMapper.writeValueAsString(any(Payment.class))).thenReturn("{\"amount\":\"100\"}");

        var redirectUrl = paymentService.processPayment(payment);

        assertEquals("http://example.com", redirectUrl);
        verify(paymentRequestRepository).save(any(PaymentRequest.class));
        verify(paymentResponseRepository).save(any(PaymentResponse.class));
    }

    @Test
    void givenNullResponseBody_whenProcessPayment_thenReturnFailureMessage() throws Exception {
        var payment = new Payment();
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        doReturn("{\"amount\":\"100\"}").when(objectMapper).writeValueAsString(paymentCaptor.capture());
        doReturn(Optional.empty()).when(httpClientService).sendPaymentRequest(anyString());

        var result = paymentService.processPayment(payment);

        assertEquals(ERR_PAYMENT_FAILED_DUE_TO_NO_RESPONSE_FROM_THE_PAYMENT_SERVICE, result);

        verify(paymentRequestRepository).save(any(PaymentRequest.class));
        verify(paymentResponseRepository, never()).save(any(PaymentResponse.class));

        Payment capturedPayment = paymentCaptor.getValue();
        assertEquals(PaymentType.DEPOSIT, capturedPayment.getPaymentType());
        assertEquals(PaymentMethod.BASIC_CARD, capturedPayment.getPaymentMethod());
    }

    @Test
    void givenJsonProcessingException_whenPrepareRequestBody_thenThrowRuntimeException() throws Exception {
        var payment = new Payment();

        doThrow(new JsonProcessingException("Serialization error") {}).when(objectMapper).writeValueAsString(any(Payment.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(payment);
        });

        assertEquals(ERR_FAILED_TO_SERIALIZE_PAYMENT_OBJECT, exception.getMessage());
    }

    @Test
    void givenValidPaymentAndErrorDuringPaymentRequestSave_whenProcessPayment_thenThrowPaymentProcessingException() {
        var payment = new Payment();
        doThrow(new PaymentProcessingException("Failed to save payment request.")).when(paymentRequestRepository).save(any(PaymentRequest.class));

        PaymentProcessingException exception = assertThrows(PaymentProcessingException.class, () -> {
            paymentService.processPayment(payment);
        });

        assertEquals(ERR_FAILED_TO_SAVE_PAYMENT_REQUEST, exception.getMessage());
    }

    @Test
    void givenValidPaymentAndErrorDuringPaymentResponseSave_whenProcessPayment_thenThrowPaymentProcessingException() throws Exception {
        var payment = Payment.builder()
                .paymentType(PaymentType.DEPOSIT)
                .paymentMethod(PaymentMethod.BASIC_CARD)
                .build();

        PaymentRequest paymentRequest = PaymentRequest.builder().build();

        when(objectMapper.writeValueAsString(eq(payment))).thenReturn("{\"amount\":\"100\"}");
        when(httpClientService.sendPaymentRequest(anyString())).thenReturn(Optional.of(httpResponse));
        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenReturn(paymentRequest);

        doThrow(new PaymentProcessingException(ERR_FAILED_TO_SAVE_PAYMENT_RESPONSE)).when(paymentResponseRepository).save(any(PaymentResponse.class));

        PaymentProcessingException exception = assertThrows(PaymentProcessingException.class, () -> {
            paymentService.processPayment(payment);
        });

        assertEquals(ERR_FAILED_TO_SAVE_PAYMENT_RESPONSE, exception.getMessage());
    }
}
