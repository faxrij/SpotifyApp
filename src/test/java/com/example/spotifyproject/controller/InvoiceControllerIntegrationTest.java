package com.example.spotifyproject.controller;

import com.example.spotifyproject.helper.TokenHelper;
import com.example.spotifyproject.model.request.PayInvoiceRequest;
import com.example.spotifyproject.model.response.CategoryResponse;
import com.example.spotifyproject.model.response.InvoiceResponse;
import com.example.spotifyproject.util.RestResponsePage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.junit.jupiter.Testcontainers;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class InvoiceControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private TokenHelper tokenHelper;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/contractRecord/create_record.sql",
            "/invoice/create_invoice.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/invoice/delete_invoice.sql", "/contractRecord/delete_record.sql",
            "/auth/delete_user.sql"})
    public void testGetInvoices() {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<RestResponsePage<CategoryResponse>> response = restTemplate.exchange(
                "/invoice",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<RestResponsePage<CategoryResponse>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(response.getBody().getContent().size(), 2);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/contractRecord/create_record.sql",
            "/invoice/create_invoice.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/invoice/delete_invoice.sql", "/contractRecord/delete_record.sql",
            "/auth/delete_user.sql"})
    public void testGetInvoices_withNonAdminUser() {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<CategoryResponse> response = restTemplate.exchange(
                "/invoice",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<CategoryResponse>() {
                });

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/contractRecord/create_record.sql",
            "/invoice/create_invoice.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/invoice/delete_invoice.sql", "/contractRecord/delete_record.sql",
            "/auth/delete_user.sql"})
    public void testGetInvoiceById() {
        String invoiceId = "41e7c96d-5d9a-4bc6-81c7-dc5a07d70f5e";

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<CategoryResponse> response = restTemplate.exchange(
                "/invoice/" + invoiceId,
                HttpMethod.GET,
                entity,
                CategoryResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(invoiceId, response.getBody().getId());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/contractRecord/create_record.sql",
            "/invoice/create_invoice.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/invoice/delete_invoice.sql", "/contractRecord/delete_record.sql",
            "/auth/delete_user.sql"})
    public void testGetInvoiceById_withNonAdminUser() {
        String invoiceId = "41e7c96d-5d9a-4bc6-81c7-dc5a07d70f5e";

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(tokenHelper.generateTokenForMember());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<InvoiceResponse> response = restTemplate.exchange(
                "/invoice/" + invoiceId,
                HttpMethod.GET,
                entity,
                InvoiceResponse.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/contractRecord/create_record.sql",
            "/invoice/create_invoice.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/invoice/delete_invoice.sql", "/contractRecord/delete_record.sql",
            "/auth/delete_user.sql"})
    public void testGetInvoiceById_whenInvoiceDoesNotExist() {
        String invoiceId = "41e7c96d-5d9a-4bc6-81c7-dc5a07d70f5e1";

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<CategoryResponse> response = restTemplate.exchange(
                "/invoice/" + invoiceId,
                HttpMethod.GET,
                entity,
                CategoryResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/contractRecord/create_record.sql",
            "/invoice/create_invoice.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/payment/delete_payment.sql", "/invoice/delete_invoice.sql", "/contractRecord/delete_record.sql",
            "/auth/delete_user.sql"})
    public void testPayInvoice() {
        String invoiceId = "a94362ed-724f-4728-9466-c310b6e42425";

        PayInvoiceRequest payInvoiceRequest = new PayInvoiceRequest();
        payInvoiceRequest.setReceiverCard("1232425353535353");
        payInvoiceRequest.setSenderCard("9789826375983753");
        payInvoiceRequest.setAmount(10);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PayInvoiceRequest> entity = new HttpEntity<>(payInvoiceRequest, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/invoice/" + invoiceId + "/pay",
                HttpMethod.POST,
                entity,
                Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
