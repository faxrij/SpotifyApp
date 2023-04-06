package com.example.spotifyproject.controller;

import com.example.spotifyproject.helper.TokenHelper;
import com.example.spotifyproject.model.request.subscription.CreateSubscriptionRequest;
import com.example.spotifyproject.model.request.subscription.UpdateSubscriptionRequest;
import com.example.spotifyproject.model.response.SubscriptionResponse;
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

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class SubscriptionControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private TokenHelper tokenHelper;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/subscription/create_subscription.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/subscription/delete_subscription.sql"})
    public void testGetSubscriptions() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<RestResponsePage<SubscriptionResponse>> response = restTemplate.exchange(
                "/subscription",
                HttpMethod.GET, entity,
                new ParameterizedTypeReference<RestResponsePage<SubscriptionResponse>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).isEmpty());
        assertEquals(3, response.getBody().getTotalElements());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/subscription/create_subscription.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/subscription/delete_subscription.sql"})
    public void testGetSubscriptionById() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<SubscriptionResponse> response = restTemplate.exchange(
                "/subscription/414c32f2-3b3d-11ec-8d3d-0242ac130003",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<SubscriptionResponse>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/subscription/create_subscription.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/subscription/delete_subscription.sql"})
    public void testAddSubscription() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());
        headers.setContentType(MediaType.APPLICATION_JSON);

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setName("Test Subscription");
        request.setDuration(12);
        request.setFee(100);

        HttpEntity<CreateSubscriptionRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/subscription",
                HttpMethod.POST, entity, Void.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/subscription/create_subscription.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/subscription/delete_subscription.sql"})
    public void testAddSubscription_nonAdminUser_shouldThrowError() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        headers.setContentType(MediaType.APPLICATION_JSON);

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setName("Test Subscription");
        request.setDuration(12);
        request.setFee(100);

        HttpEntity<CreateSubscriptionRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/subscription",
                HttpMethod.POST, entity, Void.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/subscription/create_subscription.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/subscription/delete_subscription.sql"})
    public void testUpdateSubscription() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());
        headers.setContentType(MediaType.APPLICATION_JSON);

        UpdateSubscriptionRequest request = new UpdateSubscriptionRequest();
        request.setName("Updated Test Subscription");
        request.setDuration(12);
        request.setFee(100);
        request.setIsActive(true);

        HttpEntity<UpdateSubscriptionRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/subscription/414c32f2-3b3d-11ec-8d3d-0242ac130003",
                HttpMethod.PUT, entity, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/subscription/create_subscription.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/subscription/delete_subscription.sql"})
    public void testUpdateSubscription_nonAdminUser_shouldThrowError() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        headers.setContentType(MediaType.APPLICATION_JSON);

        UpdateSubscriptionRequest request = new UpdateSubscriptionRequest();
        request.setName("Updated Test Subscription");
        request.setDuration(12);
        request.setFee(100);
        request.setIsActive(true);
        HttpEntity<UpdateSubscriptionRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/subscription/414c32f2-3b3d-11ec-8d3d-0242ac130003",
                HttpMethod.PUT, entity, Void.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/subscription/create_subscription.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/subscription/delete_subscription.sql"})
    public void testDeleteSubscription() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/subscription/414c32f2-3b3d-11ec-8d3d-0242ac130003",
                HttpMethod.DELETE, entity, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/subscription/create_subscription.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/subscription/delete_subscription.sql"})
    public void testDeleteSubscription_nonAdminUserShouldThrowError() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/subscription/5d5f5e5c-3b3d-11ec-8d3d-0242ac130003",
                HttpMethod.DELETE, entity, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
