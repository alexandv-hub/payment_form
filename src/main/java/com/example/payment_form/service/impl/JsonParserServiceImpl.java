package com.example.payment_form.service.impl;

import com.example.payment_form.exception.PaymentProcessingException;
import com.example.payment_form.service.JsonParserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.example.payment_form.messages.ErrorMessages.Validation.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class JsonParserServiceImpl implements JsonParserService {

    private final ObjectMapper objectMapper;

    @Override
    public String extractRedirectUrl(String responseBody) {
        return extractValueFromResponse(responseBody, "redirectUrl");
    }

    @Override
    public String extractResponseId(String responseBody) {
        return extractValueFromResponse(responseBody, "id");
    }

    private String extractValueFromResponse(String responseBody, String key) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<>() {});
            if (responseMap.containsKey("errors")) {
                log.error("Validation error: {}", responseMap.get("errors"));
                throw new PaymentProcessingException(ERR_VALIDATION_FAILED_ON_THE_SERVER);
            }

            Map<String, Object> resultMap = (Map<String, Object>) responseMap.get("result");
            if (resultMap == null) {
                throw new PaymentProcessingException(ERR_RESULT_IS_MISSING_IN_THE_RESPONSE);
            }

            return (String) resultMap.get(key);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(ERR_FAILED_TO_PARSE_RESPONSE_JSON, e);
        }
    }
}
