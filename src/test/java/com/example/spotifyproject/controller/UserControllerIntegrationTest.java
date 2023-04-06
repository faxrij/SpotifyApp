package com.example.spotifyproject.controller;

import com.example.spotifyproject.helper.TokenHelper;
import com.example.spotifyproject.model.request.User.UpdateUserRequest;
import com.example.spotifyproject.model.response.UserResponse;
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
public class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private TokenHelper tokenHelper;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql"})
    public void testGetUsers_withNonAdminUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/user",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Void>() {});

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql"})
    public void testGetUsers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<RestResponsePage<UserResponse>> response = restTemplate.exchange(
                "/user",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<RestResponsePage<UserResponse>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).isEmpty());
        assertEquals(2, response.getBody().getTotalElements());
    }


    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql"})
    public void testGetUserById() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<UserResponse>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("memberName", Objects.requireNonNull(response.getBody()).getName());
        assertEquals("memberLastName", Objects.requireNonNull(response.getBody()).getLastName());
        assertEquals("member@gmail.com", Objects.requireNonNull(response.getBody()).getEmail());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql"})
    public void testGetUserById_withNonAdminUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf102",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<UserResponse>() {});

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/contractRecord/create_record.sql",
            "/invoice/create_invoice.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/invoice/delete_invoice.sql", "/contractRecord/delete_record.sql",
            "/auth/delete_user.sql"})
    public void testGetInvoicesByUserId() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/invoice",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<UserResponse>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/contractRecord/create_record.sql",
            "/invoice/create_invoice.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/invoice/delete_invoice.sql", "/contractRecord/delete_record.sql",
            "/auth/delete_user.sql"})
    public void testGetInvoicesByUserId_nonExistingAccount() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf101/invoice",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<UserResponse>() {});

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/subscription/create_subscription.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/contractRecord/delete_record.sql", "/subscription/delete_subscription.sql",
            "/auth/delete_user.sql"})
    public void testSubscribe() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/subscribe/5d5f5e5c-3b3d-11ec-8d3d-0242ac130003",
                HttpMethod.POST,
                entity,
                Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql"})
    public void testUpdateUserById() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setName("User Email");
        updateUserRequest.setLastName("User LastName");
        updateUserRequest.setEmail("user@email.com");
        HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(updateUserRequest, headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf102",
                HttpMethod.PUT,
                entity,
                Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql"})
    public void testUpdateUserById_withForbiddenUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setName("User Email");
        updateUserRequest.setLastName("User LastName");
        updateUserRequest.setEmail("user@email.com");
        HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(updateUserRequest, headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf102",
                HttpMethod.PUT,
                entity,
                Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
