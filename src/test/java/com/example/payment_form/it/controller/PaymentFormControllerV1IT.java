package com.example.payment_form.it.controller;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
public class PaymentFormControllerV1IT {

    @Autowired
    private MockMvc mockMvc;

    @Value("${api.url}")
    private String apiUrl;

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
    void whenShowPaymentForm_thenDisplayForm() throws Exception {
        mockMvc.perform(get(apiUrl + "/form"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/payment_form"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("payment"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("currencies"));
    }

    @Test
    void whenProcessPayment_andValidRequest_thenRedirectToSuccess() throws Exception {
        mockMvc.perform(post(apiUrl + "/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("amount", "100")
                        .param("currency", "USD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("https://engine-sandbox.pay.tech/**"));
    }

    @Test
    void whenProcessPayment_andInvalidRequest_thenReturnErrorView() throws Exception {
        mockMvc.perform(post(apiUrl + "/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("amount", "INVALID_AMOUNT_STRING_100.500")
                        .param("currency", "INVALID_CURRENCY_STRING"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/payment_error"))
                .andExpect(MockMvcResultMatchers.model().attributeHasFieldErrors("payment", "amount", "currency"));
    }
}
