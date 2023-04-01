package com.example.spotifyproject.controller;

import com.example.spotifyproject.helper.TokenHelper;
import com.example.spotifyproject.model.request.content.CreateContentRequest;
import com.example.spotifyproject.model.request.content.UpdateContentRequest;
import com.example.spotifyproject.model.response.ContentResponse;
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
public class ContentControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private TokenHelper tokenHelper;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/content/delete_song.sql"})
    public void testGetContents() {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<RestResponsePage<ContentResponse>> response = restTemplate.exchange(
                "/content",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<RestResponsePage<ContentResponse>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(response.getBody().getContent().size(), 2);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/content/delete_song.sql"})
    public void testGetContentById() {
        String contentId = "70f72757-1ba7-4638-9360-afeb89ef6785";

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(tokenHelper.generateTokenForMember());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<ContentResponse> response = restTemplate.exchange(
                "/content/" + contentId,
                HttpMethod.GET,
                entity,
                ContentResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(contentId, response.getBody().getId());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/content/delete_song.sql"})
    public void testAddContent() {
        CreateContentRequest createContentRequest = new CreateContentRequest();
        createContentRequest.setName("Test Song");
        createContentRequest.setComposer("Test Composer");
        createContentRequest.setTitle("Test Title");
        createContentRequest.setLyrics("Test Lyrics");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateContentRequest> entity = new HttpEntity<>(createContentRequest, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/content",
                HttpMethod.POST,
                entity,
                Void.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/content/delete_song.sql"})
    public void testAddContent_whenUserIsNonAdmin() {
        CreateContentRequest createContentRequest = new CreateContentRequest();
        createContentRequest.setName("Test Song");
        createContentRequest.setComposer("Test Composer");
        createContentRequest.setTitle("Test Title");
        createContentRequest.setLyrics("Test Lyrics");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateContentRequest> entity = new HttpEntity<>(createContentRequest, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/content",
                HttpMethod.POST,
                entity,
                Void.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/content/delete_song.sql"})
    public void testUpdateContent() {
        String contentId = "70f72757-1ba7-4638-9360-afeb89ef6785";

        UpdateContentRequest updateContentRequest = new UpdateContentRequest();
        updateContentRequest.setName("Update Test Content");
        updateContentRequest.setComposerName("Update Test Composer");
        updateContentRequest.setTitle("TEST TITLE");
        updateContentRequest.setLyric("TEST LYRICS");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());

        HttpEntity<UpdateContentRequest> entity = new HttpEntity<>(updateContentRequest, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/content/" + contentId,
                HttpMethod.PUT,
                entity,
                Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/content/delete_song.sql"})
    public void testUpdateContent_whenContentDoesNotExist() {
        String contentId = "70f72757-1ba7-4638-9360-afeb89ef67851";

        UpdateContentRequest updateContentRequest = new UpdateContentRequest();
        updateContentRequest.setName("Update Test Content");
        updateContentRequest.setComposerName("Update Test Composer");
        updateContentRequest.setTitle("TEST TITLE");
        updateContentRequest.setLyric("TEST LYRICS");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());

        HttpEntity<UpdateContentRequest> entity = new HttpEntity<>(updateContentRequest, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/content/" + contentId,
                HttpMethod.PUT,
                entity,
                Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/content/delete_song.sql"})
    public void testUpdateContent_withNonAdminUser() {
        String contentId = "70f72757-1ba7-4638-9360-afeb89ef6785";

        UpdateContentRequest updateContentRequest = new UpdateContentRequest();
        updateContentRequest.setName("Update Test Content");
        updateContentRequest.setComposerName("Update Test Composer");
        updateContentRequest.setTitle("TEST TITLE");
        updateContentRequest.setLyric("TEST LYRICS");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());

        HttpEntity<UpdateContentRequest> entity = new HttpEntity<>(updateContentRequest, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/content/" + contentId,
                HttpMethod.PUT,
                entity,
                Void.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/content/delete_song.sql"})
    public void testDeleteContent() {
        String contentId = "70f72757-1ba7-4638-9360-afeb89ef6785";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/content/" + contentId,
                HttpMethod.DELETE,
                entity,
                Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/content/delete_song.sql"})
    public void testDeleteContent_withNonAdminUser() {
        String contentId = "70f72757-1ba7-4638-9360-afeb89ef6785";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/content/" + contentId,
                HttpMethod.DELETE,
                entity,
                Void.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/auth/delete_user.sql", "/content/delete_song.sql"})
    public void testDeleteContent_whenContentDoesNotExist() {
        String contentId = "70f72757-1ba7-4638-9360-afeb89ef67851";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/content/" + contentId,
                HttpMethod.DELETE,
                entity,
                Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
