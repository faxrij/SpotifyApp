package com.example.spotifyproject.service;


import com.example.spotifyproject.entity.Invoice;
import com.example.spotifyproject.entity.Role;
import com.example.spotifyproject.entity.Song;
import com.example.spotifyproject.entity.User;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.model.request.content.CreateContentRequest;
import com.example.spotifyproject.model.request.content.UpdateContentRequest;
import com.example.spotifyproject.model.response.ContentResponse;
import com.example.spotifyproject.model.response.InvoiceResponse;
import com.example.spotifyproject.repository.ContentRepository;
import com.example.spotifyproject.repository.InvoiceRepository;
import com.example.spotifyproject.repository.UserRepository;
import com.example.spotifyproject.service.mapper.FromSongToContentResponse;
import com.example.spotifyproject.util.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContentServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContentService contentService;
    @Mock
    private FromSongToContentResponse fromSongToContentResponse;
    @Mock
    private InvoiceRepository invoiceRepository;
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId("1");
        user.setRole(Role.MEMBER);
    }

    @Test
    public void testGetAllContent() {
        // given
        when(userRepository.findById("123")).thenReturn(Optional.of(user));

        Song song = new Song();
        song.setId("123");
        song.setName("Test Category");
        song.setModifiedDate(DateUtil.now());
        song.setCreatedDate(DateUtil.now());

        ContentResponse contentResponse = new ContentResponse();

        contentResponse.setTitle(song.getTitle());
        contentResponse.setName(song.getName());
        contentResponse.setCreatedDate(song.getCreatedDate());
        contentResponse.setModifiedDate(song.getModifiedDate());
        contentResponse.setLyrics(song.getLyrics());
        contentResponse.setComposerName(song.getComposerName());
        contentResponse.setId(song.getId());

        Page<Song> songPage = new PageImpl<>(Collections.singletonList(song));

        when(contentRepository.findAllContentLike(Pageable.unpaged(), "%%","%%","%%","%%")).thenReturn(songPage);

        when(fromSongToContentResponse.fromSongToContentResponse(any(Song.class))).thenReturn(contentResponse);


        Page<ContentResponse> result = contentService.getAllContent(Pageable.unpaged(),"","","","","123");

        // then
        assertFalse(result.getContent().isEmpty());
        assertEquals(result.getContent().get(0).getId(), song.getId());
        assertEquals(result.getContent().get(0).getName(), song.getName());
        assertEquals(result.getContent().get(0).getCreatedDate(), song.getCreatedDate());
        assertEquals(result.getContent().get(0).getModifiedDate(), song.getModifiedDate());
        assertEquals(result.getContent().get(0).getTitle(), song.getTitle());
        assertEquals(result.getContent().get(0).getLyrics(), song.getLyrics());
        assertEquals(result.getContent().get(0).getComposerName(), song.getComposerName());

    }

    @Test
    public void testGetAllContent_unauthorized() {
        // Mock input parameters
        Pageable pageable = PageRequest.of(0, 10);
        String name = "testName";
        String lyrics = "testLyrics";
        String title = "testTitle";
        String composerName = "testComposerName";
        String userId = "testUserId";

        // Mock User
        User user = new User();
        user.setRole(Role.GUEST);

        // Mock userRepository
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Call method
        assertThrows(BusinessException.class, () -> contentService.getAllContent(pageable, name, lyrics, title, composerName, userId));
    }

    @Test
    public void testGetContentById() {
        // given
        String contentId = "123";
        Song song = new Song();
        song.setId(contentId);
        song.setName("Test Song");
        song.setTitle("Test Title");
        song.setLyrics("Test Lyrics");
        song.setComposerName("Test Composer");

        ContentResponse contentResponse = new ContentResponse();

        contentResponse.setTitle(song.getTitle());
        contentResponse.setName(song.getName());
        contentResponse.setCreatedDate(song.getCreatedDate());
        contentResponse.setModifiedDate(song.getModifiedDate());
        contentResponse.setLyrics(song.getLyrics());
        contentResponse.setComposerName(song.getComposerName());
        contentResponse.setId(song.getId());

        when(userRepository.findById("123")).thenReturn(Optional.of(user));
        when(contentRepository.findById(contentId)).thenReturn(Optional.of(song));
        when(fromSongToContentResponse.fromSongToContentResponse(any(Song.class))).thenReturn(contentResponse);

        // when
        ContentResponse result = contentService.getContentById(contentId, "123");

        // then
        assertNotNull(result);
        assertEquals(song.getName(), result.getName());
        assertEquals(song.getTitle(), result.getTitle());
        assertEquals(song.getLyrics(), result.getLyrics());
        assertEquals(song.getComposerName(), result.getComposerName());
    }

    @Test
    public void testGetContentById_shouldThrowError_whenSongIsNotFound() {
        when(userRepository.findById("123")).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> contentService.getContentById("123", "123"));
    }

    @Test
    public void testGetContentById_shouldThrowError_whenUserIsNotFound() {

        assertThrows(BusinessException.class, () -> contentService.getContentById("123", "123"));
    }

    @Test
    public void testAddContent() {
        // given
        CreateContentRequest request = new CreateContentRequest();
        request.setName("Song name");
        request.setTitle("Song title");
        request.setLyrics("Song lyrics");
        request.setComposer("Song composer");

        user.setRole(Role.ADMIN);
        Mockito.when(userRepository.findById("1")).thenReturn(Optional.of(user));

        // when
        contentService.addContent(request, "1");

        // then
        ArgumentCaptor<Song> songArgumentCaptor = ArgumentCaptor.forClass(Song.class);
        Mockito.verify(contentRepository, Mockito.times(1)).save(songArgumentCaptor.capture());
        Song savedSong = songArgumentCaptor.getValue();

        assertNotNull(savedSong);
        assertEquals("Song name", savedSong.getName());
        assertEquals("Song title", savedSong.getTitle());
        assertEquals("Song lyrics", savedSong.getLyrics());
        assertEquals("Song composer", savedSong.getComposerName());
    }

    @Test
    public void testAddContent_shouldThrowError() {
        // given
        CreateContentRequest request = new CreateContentRequest();

        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        // when + then
        assertThrows(BusinessException.class, () -> contentService.addContent(request, "1"));

    }

    @Test
    public void testUpdateContent() {
        // given
        String songId = "song123";
        Song song = new Song();
        song.setId(songId);
        song.setName("Old Name");
        song.setTitle("Old Title");
        song.setLyrics("Old Lyrics");
        song.setComposerName("Old Composer");
        contentRepository.save(song);

        UpdateContentRequest updateRequest = new UpdateContentRequest();
        updateRequest.setName("New Name");
        updateRequest.setTitle("New Title");
        updateRequest.setLyric("New Lyrics");
        updateRequest.setComposerName("New Composer");

        user.setRole(Role.ADMIN);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(contentRepository.findById(songId)).thenReturn(Optional.of(song));


        // when
        contentService.updateContent(songId, updateRequest, "1");

        // then
        Song updatedSong = contentRepository.findById(songId).orElse(null);
        assert updatedSong != null;
        assertNotNull(updatedSong);
        assertEquals("New Name", updatedSong.getName());
        assertEquals("New Title", updatedSong.getTitle());
        assertEquals("New Lyrics", updatedSong.getLyrics());
        assertEquals("New Composer", updatedSong.getComposerName());
    }

    @Test
    void testDeleteContent() {
        // create a new song
        Song song = new Song();
        song.setName("Song Name");
        song.setTitle("Song Title");
        song.setLyrics("Song Lyrics");
        song.setComposerName("Composer Name");
        song.setId("1");
        song.setCreatedDate(DateUtil.now());
        song.setModifiedDate(DateUtil.now());
        contentRepository.save(song);

        user.setRole(Role.ADMIN);
        userRepository.save(user);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(contentRepository.findById("1")).thenReturn(Optional.of(song));

        contentService.deleteContent("1", "1");

        verify(contentRepository).deleteFromTableJointWithCategories("1");
        verify(contentRepository).deleteFromTableJointWithUsers("1");
        verify(contentRepository).delete(song);

        assertFalse(contentRepository.existsById("1"));
    }
}