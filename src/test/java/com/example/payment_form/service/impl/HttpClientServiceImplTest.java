package com.example.payment_form.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class HttpClientServiceImplTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpClient.Builder httpClientBuilder;

    @Mock
    private HttpResponse<String> httpResponse;

    @InjectMocks
    private HttpClientServiceImpl httpClientService;

    private static final String API_URL = "https://some-test.com/api/v1/payments";
    private static final String BEARER_TOKEN = "test_Bearer_token";
    private static final int CONNECT_TIMEOUT = 5;
    private static final int REQUEST_TIMEOUT = 10;
    private static final String REQUEST_BODY = "{\"test\":\"data\"}";
    private static final int MAX_RETRIES = 3;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(httpClientService, "apiUrl", API_URL);
        ReflectionTestUtils.setField(httpClientService, "bearerToken", BEARER_TOKEN);
        ReflectionTestUtils.setField(httpClientService, "connectTimeOut", CONNECT_TIMEOUT);
        ReflectionTestUtils.setField(httpClientService, "requestTimeOut", REQUEST_TIMEOUT);
        ReflectionTestUtils.setField(httpClientService, "maxRetries", MAX_RETRIES);
    }

    @Test
    public void whenSendPaymentRequest_thenSuccessResponseAndVerifySendIsCalled() throws IOException, InterruptedException {
        var expectedResponse = "{\"foo\": \"bar\"}";
        when(httpResponse.body()).thenReturn(expectedResponse);

        when(httpClientBuilder.connectTimeout(any(Duration.class))).thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(httpClient);

        try (MockedStatic<HttpClient> httpClientMockedStatic = mockStatic(HttpClient.class)) {
            httpClientMockedStatic.when(HttpClient::newBuilder).thenReturn(httpClientBuilder);
            httpClientMockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);

            when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                    .thenReturn(httpResponse);

            Optional<HttpResponse<String>> actualResponse = httpClientService.sendPaymentRequest(REQUEST_BODY);

            assertTrue(actualResponse.isPresent(), "Response should be present");
            assertEquals(expectedResponse, actualResponse.get().body());

            verify(httpClient, times(1)).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
        }
    }

    @Test
    void whenSendPaymentRequest_thenTimeoutSettingsApplied() throws IOException, InterruptedException {
        when(httpClientBuilder.connectTimeout(any(Duration.class))).thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(httpClient);

        try (MockedStatic<HttpClient> httpClientMockedStatic = mockStatic(HttpClient.class)) {
            httpClientMockedStatic.when(HttpClient::newBuilder).thenReturn(httpClientBuilder);
            httpClientMockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);

            when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                    .thenReturn(httpResponse);
            when(httpResponse.body()).thenReturn("Success");

            httpClientService.sendPaymentRequest(REQUEST_BODY);

            ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient, times(1)).send(requestCaptor.capture(), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());

            HttpRequest actualRequest = requestCaptor.getValue();
            assertEquals(Duration.ofSeconds(REQUEST_TIMEOUT), actualRequest.timeout().get());
            assertEquals(URI.create(API_URL), actualRequest.uri());
        }
    }

    @Test
    void whenSendPaymentRequest_thenRetriesAndFails() throws IOException, InterruptedException {
        var requestBody = "{\"key\":\"value\"}";

        when(httpClientBuilder.connectTimeout(any(Duration.class))).thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(httpClient);

        try (MockedStatic<HttpClient> httpClientMockedStatic = mockStatic(HttpClient.class)) {
            httpClientMockedStatic.when(HttpClient::newBuilder).thenReturn(httpClientBuilder);
            httpClientMockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);

            when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                    .thenThrow(new IOException("Simulated IO exception"));

            Optional<HttpResponse<String>> response = httpClientService.sendPaymentRequest(requestBody);

            assertFalse(response.isPresent(), "Response should not be present");

            verify(httpClient, times(3)).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
        }
    }

    @Test
    void whenSendPaymentRequest_thenSuccessOnSecondAttempt() throws IOException, InterruptedException {
        var requestBody = "{\"key\":\"value\"}";

        when(httpClientBuilder.connectTimeout(any(Duration.class))).thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(httpClient);

        try (MockedStatic<HttpClient> httpClientMockedStatic = mockStatic(HttpClient.class)) {
            httpClientMockedStatic.when(HttpClient::newBuilder).thenReturn(httpClientBuilder);
            httpClientMockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);

            when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                    .thenThrow(new IOException("Simulated IO exception"))
                    .thenReturn(httpResponse);
            when(httpResponse.body()).thenReturn("{\"status\":\"success\"}");

            var responseOptional = httpClientService.sendPaymentRequest(requestBody);

            assertTrue(responseOptional.isPresent(), "Response should be present");
            assertEquals("{\"status\":\"success\"}", responseOptional.get().body());

            verify(httpClient, times(2)).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
        }
    }

    @Test
    void givenRequestWithNoAcceptHeader_whenRequestIsExecuted_thenDefaultResponseContentTypeIsJson() throws Exception {
        var requestBody = "{\"key\":\"value\"}";

        var buildHttpRequestMethod = HttpClientServiceImpl.class.getDeclaredMethod("buildHttpRequest", String.class);
        buildHttpRequestMethod.setAccessible(true);

        var httpRequest = (HttpRequest) buildHttpRequestMethod.invoke(httpClientService, requestBody);

        assertEquals("application/json", httpRequest.headers().firstValue("accept").orElse(""));
        assertEquals("application/json", httpRequest.headers().firstValue("content-type").orElse(""));
        assertEquals("Bearer " + BEARER_TOKEN, httpRequest.headers().firstValue("authorization").orElse(""));
    }

    @Test
    void givenRequestWithTimeout_whenRequestIsExecuted_thenRequestTimeoutIsSet() throws Exception {
        var requestBody = "{\"key\":\"value\"}";

        var buildHttpRequestMethod = HttpClientServiceImpl.class.getDeclaredMethod("buildHttpRequest", String.class);
        buildHttpRequestMethod.setAccessible(true);

        var httpRequest = (HttpRequest) buildHttpRequestMethod.invoke(httpClientService, requestBody);

        assertEquals(Duration.ofSeconds(REQUEST_TIMEOUT), httpRequest.timeout().orElse(Duration.ZERO));
    }
}
