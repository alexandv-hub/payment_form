package com.example.payment_form.service.impl;

import static com.example.payment_form.messages.ErrorMessages.Validation.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.payment_form.exception.PaymentProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsonParserServiceImplTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JsonParserServiceImpl jsonParserService;

    private final String responseBody = "{\"result\":{\"redirectUrl\":\"http://example.com\"}}";
    private final String responseBodyWithError = "{\"errors\":\"Some validation error\"}";
    private final String invalidResponseBody = "{\"result\":{\"id\":123}}";

    @Test
    void givenValidResponseBody_whenExtractRedirectUrl_thenReturnRedirectUrl() throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("redirectUrl", "http://example.com");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("result", resultMap);

        doReturn(responseMap).when(objectMapper).readValue(eq(responseBody), ArgumentMatchers.<TypeReference<Map<String, Object>>>any());

        String redirectUrl = jsonParserService.extractRedirectUrl(responseBody);

        assertEquals("http://example.com", redirectUrl);
    }

    @Test
    void givenResponseBodyWithErrors_whenExtractRedirectUrl_thenThrowPaymentProcessingException() throws Exception {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("errors", "Some validation error");

        doReturn(responseMap).when(objectMapper).readValue(eq(responseBodyWithError), ArgumentMatchers.<TypeReference<Map<String, Object>>>any());

        PaymentProcessingException exception = assertThrows(PaymentProcessingException.class, () -> jsonParserService.extractRedirectUrl(responseBodyWithError));

        assertEquals(ERR_VALIDATION_FAILED_ON_THE_SERVER, exception.getMessage());
    }

    @Test
    void givenInvalidResponseBody_whenExtractRedirectUrl_thenThrowPaymentProcessingException() throws Exception {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("result", null);

        doReturn(responseMap).when(objectMapper).readValue(eq(invalidResponseBody), ArgumentMatchers.<TypeReference<Map<String, Object>>>any());

        PaymentProcessingException exception = assertThrows(PaymentProcessingException.class, () -> jsonParserService.extractRedirectUrl(invalidResponseBody));

        assertEquals(ERR_RESULT_IS_MISSING_IN_THE_RESPONSE, exception.getMessage());
    }

    @Test
    void givenJsonProcessingException_whenExtractRedirectUrl_thenThrowRuntimeException() throws Exception {
        doThrow(new JsonProcessingException("Parsing error") {}).when(objectMapper).readValue(eq(responseBody), ArgumentMatchers.<TypeReference<Map<String, Object>>>any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> jsonParserService.extractRedirectUrl(responseBody));

        assertEquals(ERR_FAILED_TO_PARSE_RESPONSE_JSON, exception.getMessage());
    }
}
