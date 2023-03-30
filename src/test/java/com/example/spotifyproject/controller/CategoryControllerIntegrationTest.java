package com.example.spotifyproject.controller;

import com.example.spotifyproject.helper.TokenHelper;
import com.example.spotifyproject.model.request.category.CreateCategoryRequest;
import com.example.spotifyproject.model.request.category.UpdateCategoryRequest;
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

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class CategoryControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private TokenHelper tokenHelper;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/category/create_category.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/category/delete_category.sql"})
    public void testGetCategories() {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<RestResponsePage<CategoryResponse>> response = restTemplate.exchange(
                "/category",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<RestResponsePage<CategoryResponse>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(response.getBody().getContent().size(), 2);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/category/create_category.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/category/delete_category.sql"})
    public void testGetCategoryById() {
        String categoryId = "4f9b9c41-fde7-425a-8f12-7eaeef31c57f";

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(tokenHelper.generateTokenForMember());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<CategoryResponse> response = restTemplate.exchange(
                "/category/" + categoryId,
                HttpMethod.GET,
                entity,
                CategoryResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(categoryId, response.getBody().getId());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/category/delete_category.sql"})
    public void testAddCategory() {
        CreateCategoryRequest createCategoryRequest = new CreateCategoryRequest();
        createCategoryRequest.setName("Test Category");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateCategoryRequest> entity = new HttpEntity<>(createCategoryRequest, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/category",
                HttpMethod.POST,
                entity,
                Void.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/category/create_category.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/category/delete_category.sql"})
    public void testAddCategoryByParentId() {
        String parentId = "f9db9ebc-62e5-49f5-8df5-f1c57ef06d87";

        CreateCategoryRequest createCategoryRequest = new CreateCategoryRequest();
        createCategoryRequest.setName("Test Category");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());

        HttpEntity<CreateCategoryRequest> entity = new HttpEntity<>(createCategoryRequest, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/category/" + parentId,
                HttpMethod.POST,
                entity,
                Void.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/category/create_category.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/category/delete_category.sql"})
    public void testUpdateCategory() {
        String categoryId = "f9db9ebc-62e5-49f5-8df5-f1c57ef06d87";

        UpdateCategoryRequest updateCategoryRequest = new UpdateCategoryRequest();
        updateCategoryRequest.setName("New Test Category");
        updateCategoryRequest.setParentId("4f9b9c41-fde7-425a-8f12-7eaeef31c57f");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());

        HttpEntity<UpdateCategoryRequest> entity = new HttpEntity<>(updateCategoryRequest, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/category/" + categoryId,
                HttpMethod.PUT,
                entity,
                Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/category/create_category.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/category/delete_category.sql"})
    public void testDeleteCategory() {
        String categoryId = "f9db9ebc-62e5-49f5-8df5-f1c57ef06d87";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/category/" + categoryId,
                HttpMethod.DELETE,
                entity,
                Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

    }
}