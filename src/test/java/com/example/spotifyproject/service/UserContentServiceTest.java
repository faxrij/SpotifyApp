package com.example.spotifyproject.service;

import com.example.spotifyproject.entity.Role;
import com.example.spotifyproject.entity.Song;
import com.example.spotifyproject.entity.User;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.model.response.ContentResponse;
import com.example.spotifyproject.repository.ContentRepository;
import com.example.spotifyproject.repository.UserRepository;
import com.example.spotifyproject.service.mapper.FromSongToContentResponse;
import com.example.spotifyproject.util.DateUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserContentServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private FromSongToContentResponse fromSongToContentResponse;

    @InjectMocks
    private UserContentService userContentService;

    @Test
    public void testGetUserCategories() {
        // given
        String id = "1";
        String userId = "1";
        Song song1 = new Song();
        song1.setName("Song 1");
        song1.setId("1");
        
        Song song2 = new Song();
        song2.setId("2");
        song2.setName("Category 2");
        List<String> songIds = new ArrayList<>();
        songIds.add(song1.getId());
        songIds.add(song2.getId());

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        when(userRepository.findSongsByUserId(any(Pageable.class), eq("1"))).thenReturn(new PageImpl<>(songIds));

        when(contentRepository.findById("1")).thenReturn(Optional.of(song1));
        when(contentRepository.findById("2")).thenReturn(Optional.of(song2));

        when(fromSongToContentResponse.fromSongToContentResponse(song1)).thenReturn(new ContentResponse());
        when(fromSongToContentResponse.fromSongToContentResponse(song2)).thenReturn(new ContentResponse());

        Page<ContentResponse> result = userContentService.getUserContents(Pageable.unpaged(), id, userId);

        assertEquals(2, result.getTotalElements());
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userRepository).findSongsByUserId(any(Pageable.class), eq(id));
        Mockito.verify(contentRepository).findById("1");
        Mockito.verify(contentRepository).findById("2");
        Mockito.verify(fromSongToContentResponse).fromSongToContentResponse(song1);
        verify(fromSongToContentResponse).fromSongToContentResponse(song2);
    }

    @Test
    public void testGetUserCategoriesWithInvalidUserId() {
        // given
        String id = "1";
        String userId = "2";

        // when + then
        BusinessException exception = assertThrows(BusinessException.class, () -> userContentService.getUserContents(Pageable.unpaged(), id, userId));
        assertEquals("forbidden", exception.getErrorCode());
    }

    @Test
    public void testGetUserCategoriesWithUnauthorizedUser() {
        // given
        String id = "1";
        String userId = "1";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> userContentService.getUserContents(Pageable.unpaged(), id, userId));
        assertEquals("account_missing", exception.getErrorCode());
    }

    @Test
    public void testGetUserCategoryByContentId_Success() {
        String id = "1";
        String userId = "1";
        Song song1 = new Song();
        song1.setName("Song 1");
        song1.setId("1");

        List<String> songIds = new ArrayList<>();
        songIds.add(song1.getId());

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(userRepository.findSongsByUserIdAndBySongId(userId, song1.getId())).thenReturn(songIds);
        when(contentRepository.findById(id)).thenReturn(Optional.of(song1));
        when(fromSongToContentResponse.fromSongToContentResponse(song1)).thenReturn(new ContentResponse());

        ContentResponse result = userContentService.getUserContentsByContentId(userId, id, userId);

        assertNotNull(result);
    }

    @Test
    public void testGetUserCategoryByContentId_UserNotFound() {
        when(userRepository.findById(any(String.class))).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> userContentService.getUserContentsByContentId("1", "1", "1"));
    }

    @Test
    public void testGetUserCategoryByContentId_UserNotAllowed() {
        assertThrows(BusinessException.class, () -> userContentService.getUserContentsByContentId("1", "1", "2"));
    }

    @Test
    public void testGetUserCategoryByContentId_CategoryNotFound() {
        when(userRepository.findById("1")).thenReturn(Optional.of(new User()));

        assertThrows(BusinessException.class, () -> userContentService.getUserContentsByContentId("1", "cat", "1"));
    }

    @Test
    void testUserLikeCategoryById() {
        // given
        User user = new User();
        user.setId("1");
        user.setRole(Role.ADMIN);
        user.setEmail("user@gmail.com");

        user.setSongs(new ArrayList<>());
        Song song = new Song();
        song.setId("3");
        song.setName("Song");
        song.setModifiedDate(DateUtil.now());
        song.setCreatedDate(DateUtil.now());

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(contentRepository.findById("3")).thenReturn(Optional.of(song));

        // when
        userContentService.userLikeSongById("1", "3", "1");

        // then
        verify(userRepository).likeSongByUserIdAndSongId("1", "3");
    }

    @Test
    void testUserLikeCategoryByIdWhenNoMatch() {
        assertThrows(BusinessException.class, () -> userContentService.userLikeSongById("1", "3", "2"));
    }

    @Test
    void testUserLikeCategoryByIdWhenCategoryDoesNotExist() {
        when(userRepository.findById("1")).thenReturn(Optional.of(new User()));
        when(contentRepository.findById(anyString())).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> userContentService.userLikeSongById("1", "3", "1"));
    }

    @Test
    void testUserLikeCategoryByIdWhenCategoryIsAlreadyLiked() {
        // given
        User user = new User();
        user.setId("1");
        user.setRole(Role.ADMIN);
        user.setEmail("user@gmail.com");

        Song song = new Song();
        song.setId("3");
        song.setName("Category");
        song.setModifiedDate(DateUtil.now());
        song.setCreatedDate(DateUtil.now());

        List<Song> songs = new ArrayList<>();
        songs.add(song);
        user.setSongs(songs);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(contentRepository.findById("3")).thenReturn(Optional.of(song));

        // when + then
        assertThrows(BusinessException.class, () -> userContentService.userLikeSongById("1", "3", "1"));
    }

    @Test
    void testUserRemoveLikedCategoryByIdWhenNoMatch() {
        assertThrows(BusinessException.class, () -> userContentService.userRemoveLikedSongById("1", "3", "2"));
    }

    @Test
    void testUserRemoveLikedCategoryByIdWhenUserDoesNotExist() {
        when(userRepository.findById("1")).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> userContentService.userRemoveLikedSongById("1", "3", "1"));
    }

    @Test
    void testUserRemoveLikedCategoryByIdWhenCategoryExists() {
        // given
        User user = new User();
        user.setId("1");
        Song song = new Song();
        song.setId("3");
        song.setName("Category");
        song.setModifiedDate(DateUtil.now());
        song.setCreatedDate(DateUtil.now());

        List<Song> songs = new ArrayList<>();
        user.setSongs(songs);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(contentRepository.findById(anyString())).thenReturn(Optional.of(song));
        assertThrows(BusinessException.class, () -> userContentService.userRemoveLikedSongById("1", "3", "1"));
    }

    @Test
    void testUserRemoveLikedCategoryById() {
        // given
        User user = new User();
        user.setId("1");
        user.setEmail("testUser@gmail.com");
        user.setRole(Role.MEMBER);
        user.setVerified(true);

        user.setSongs(new ArrayList<>());
        Song song = new Song();
        song.setId("1");
        song.setName("testCategory");

        user.getSongs().add(song);

        // when
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(contentRepository.findById("1")).thenReturn(Optional.of(song));

        userContentService.userRemoveLikedSongById("1", "1", "1");
        verify(userRepository).removeLikedSongByUserIdAndSongId("1", "1");
    }


    @Test
    void testUserRemoveLikedCategoryByIdWhenCategoryDoesNotExist() {
        // given
        User user = new User();
        user.setId("1");
        user.setEmail("testUser@gmail.com");
        user.setRole(Role.MEMBER);
        user.setVerified(true);

        Song song = new Song();
        song.setId("1");
        song.setName("testCategory");
        List<Song> songs = new ArrayList<>();
        songs.add(song);
        user.setSongs(songs);
        user.getSongs().add(song);

        // when
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(contentRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> userContentService.userRemoveLikedSongById("1", "1", "1"));
    }
}
