package com.example.spotifyproject.controller;

import com.example.spotifyproject.helper.TokenHelper;
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

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class UserContentControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private TokenHelper tokenHelper;


    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql",
            "/userLikedSongs/create.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedSongs/delete.sql", "/content/delete_song.sql",
            "/auth/delete_user.sql"})
    public void testGetUserSongs() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<RestResponsePage<ContentResponse>> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/content",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<RestResponsePage<ContentResponse>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).isEmpty());
        assertEquals(2, response.getBody().getTotalElements());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql",
            "/userLikedSongs/create.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedSongs/delete.sql", "/content/delete_song.sql",
            "/auth/delete_user.sql"})
    public void testGetUserSongById() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<ContentResponse> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/content/70f72757-1ba7-4638-9360-afeb89ef6785",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ContentResponse>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("70f72757-1ba7-4638-9360-afeb89ef6785", Objects.requireNonNull(response.getBody()).getId());
        assertEquals("Stairway to Heaven", Objects.requireNonNull(response.getBody()).getName());
        assertEquals("Stairway to Heaven", Objects.requireNonNull(response.getBody()).getTitle());
        assertEquals("Alan Walker", Objects.requireNonNull(response.getBody()).getComposerName());
        assertFalse(Objects.requireNonNull(response.getBody()).getLyrics().isEmpty());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedSongs/delete.sql", "/content/delete_song.sql",
            "/auth/delete_user.sql"})
    public void testUserLikeSongById() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/content/70f72757-1ba7-4638-9360-afeb89ef6785/favorite",
                HttpMethod.POST,
                entity,
                Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedSongs/delete.sql", "/content/delete_song.sql",
            "/auth/delete_user.sql"})
    public void testUserLikeSongById_withOtherUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/content/70f72757-1ba7-4638-9360-afeb89ef6785/favorite",
                HttpMethod.POST,
                entity,
                Void.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedSongs/delete.sql", "/content/delete_song.sql",
            "/auth/delete_user.sql"})
    public void testUserLikeSongById_whenSongDoesNotExist() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/content/70f72757-1ba7-4638-9360-afeb89ef67853/favorite",
                HttpMethod.POST,
                entity,
                Void.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql",
            "/userLikedSongs/create.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedSongs/delete.sql", "/content/delete_song.sql",
            "/auth/delete_user.sql"})
    public void testUserRemoveLikedSongById() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/content/70f72757-1ba7-4638-9360-afeb89ef6785/unfavorite",
                HttpMethod.POST,
                entity,
                Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql",
            "/userLikedSongs/create.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedSongs/delete.sql", "/content/delete_song.sql",
            "/auth/delete_user.sql"})
    public void testUserRemoveLikedSongById_withOtherUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForAdmin());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/content/70f72757-1ba7-4638-9360-afeb89ef6785/unfavorite",
                HttpMethod.POST,
                entity,
                Void.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, value = {"/auth/create_user.sql", "/content/create_song.sql",
            "/userLikedSongs/create.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, value = {"/userLikedSongs/delete.sql", "/content/delete_song.sql",
            "/auth/delete_user.sql"})
    public void testUserRemoveLikedSongById_withNonLikedSong() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenHelper.generateTokenForMember());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/user/39d525c8-25a5-4b7c-b6e1-6aa0132cf104/content/70f72757-1ba7-4638-9360-afeb89ef67851/unfavorite",
                HttpMethod.POST,
                entity,
                Void.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
