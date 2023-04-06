package com.example.spotifyproject.controller;

import com.example.spotifyproject.helper.TokenHelper;
import com.example.spotifyproject.model.response.CategoryResponse;
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

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class UserCategoryControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private TokenHelper tokenHelper;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/category/create_category.sql",
            "/userLikedCategories/create.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedCategories/delete.sql", "/category/delete_category.sql",
            "/auth/delete_user.sql",})
    public void testGetUserCategories() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<RestResponsePage<CategoryResponse>> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/category",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<RestResponsePage<CategoryResponse>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).isEmpty());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/category/create_category.sql",
            "/userLikedCategories/create.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedCategories/delete.sql", "/category/delete_category.sql",
            "/auth/delete_user.sql",})
    public void testGetUserCategoryById() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<CategoryResponse> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/category/4f9b9c41-fde7-425a-8f12-7eaeef31c57f",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<CategoryResponse>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("4f9b9c41-fde7-425a-8f12-7eaeef31c57f", Objects.requireNonNull(response.getBody()).getId());
        assertEquals("Rock", Objects.requireNonNull(response.getBody()).getName());
        assertNull(Objects.requireNonNull(response.getBody()).getParent());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/category/create_category.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedCategories/delete.sql", "/category/delete_category.sql",
            "/auth/delete_user.sql",})
    public void testUserLikeCategoryById() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/category/4f9b9c41-fde7-425a-8f12-7eaeef31c57f/follow",
                HttpMethod.POST,
                entity,
                Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/category/create_category.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedCategories/delete.sql", "/category/delete_category.sql",
            "/auth/delete_user.sql",})
    public void testUserLikeCategoryById_withOtherUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/category/4f9b9c41-fde7-425a-8f12-7eaeef31c57f/follow",
                HttpMethod.POST,
                entity,
                Void.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/category/create_category.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedCategories/delete.sql", "/category/delete_category.sql",
            "/auth/delete_user.sql",})
    public void testUserLikeCategoryById_whenCategoryDoesNotExist() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/category/4f9b9c41-fde7-425a-8f12-7eaeef31c57f1/follow",
                HttpMethod.POST,
                entity,
                Void.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/category/create_category.sql",
            "/userLikedCategories/create.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedCategories/delete.sql", "/category/delete_category.sql",
            "/auth/delete_user.sql",})
    public void testUserRemoveLikedCategoryById() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/category/4f9b9c41-fde7-425a-8f12-7eaeef31c57f/unfollow",
                HttpMethod.POST,
                entity,
                Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/category/create_category.sql",
            "/userLikedCategories/create.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedCategories/delete.sql", "/category/delete_category.sql",
            "/auth/delete_user.sql",})
    public void testUserRemoveLikedCategoryById_withOtherUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf102/category/4f9b9c41-fde7-425a-8f12-7eaeef31c57f/unfollow",
                HttpMethod.POST,
                entity,
                Void.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/category/create_category.sql",
            "/userLikedCategories/create.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedCategories/delete.sql", "/category/delete_category.sql",
            "/auth/delete_user.sql",})
    public void testUserRemoveLikedCategoryById_withNonLikedCategory() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/category/4f9b9c41-fde7-425a-8f12-7eaeef31c57f2/unfollow",
                HttpMethod.POST,
                entity,
                Void.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
