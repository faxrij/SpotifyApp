package com.example.spotifyproject.service;

import com.example.spotifyproject.entity.*;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.exception.ErrorCode;
import com.example.spotifyproject.model.request.content.CreateContentRequest;
import com.example.spotifyproject.model.request.content.UpdateContentRequest;
import com.example.spotifyproject.model.response.ContentResponse;
import com.example.spotifyproject.repository.ContentRepository;
import com.example.spotifyproject.repository.UserRepository;
import com.example.spotifyproject.service.mapper.FromSongToContentResponse;
import com.example.spotifyproject.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ContentService {
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final FromSongToContentResponse fromSongToContentResponse;

    public Page<ContentResponse> getAllContent(Pageable pageable, String name, String lyrics, String title ,String composerName, String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "User does not exist")
        );

        if (!(user.getRole().equals(Role.MEMBER) || user.getRole().equals(Role.ADMIN))) {
            throw new BusinessException(ErrorCode.unauthorized, "User is not authorized");
        }

        Page<Song> songs = contentRepository.findAllContentLike(pageable,
                "%".concat(name).concat("%"),
                "%".concat(lyrics).concat("%"),
                "%".concat(title).concat("%"),
                "%".concat(composerName).concat("%"));

        return songs.map(fromSongToContentResponse::fromSongToContentResponse);
    }

    public ContentResponse getContentById(String contentId, String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "User does not exist")
        );

        if (!(user.getRole().equals(Role.MEMBER) || user.getRole().equals(Role.ADMIN))) {
            throw new BusinessException(ErrorCode.unauthorized, "User is not authorized");
        }

        Song song = contentRepository.findById(contentId).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "Song with provided id does not exist")
        );
        return fromSongToContentResponse.fromSongToContentResponse(song);

    }

    public void addContent(CreateContentRequest request, String id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "User does not exist")
        );

        if (! user.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.unauthorized, "User is not authorized");
        }

        Song song = new Song();
        song.setName(request.getName());
        song.setTitle(request.getTitle());
        song.setLyrics(request.getLyrics());
        song.setComposerName(request.getComposer());
        song.setCreatedDate(DateUtil.now());
        song.setModifiedDate(DateUtil.now());
        contentRepository.save(song);
    }

    public void updateContent(String id, UpdateContentRequest request, String userId) {
        Song song = checker(id, userId);

        song.setModifiedDate(DateUtil.now());
        song.setName(request.getName());
        song.setTitle(request.getTitle());
        song.setComposerName(request.getComposerName());
        song.setLyrics(request.getLyric());

        contentRepository.save(song);
    }

    private Song checker(String id, String userId) {
        Song song = contentRepository.findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "Song does not exist")
        );

        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "User does not exist")
        );

        if (! user.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.unauthorized, "User is not authorized");
        }
        return song;
    }

    public void deleteContent(String id, String userId) {
        Song song = checker(id, userId);
        contentRepository.deleteFromTableJointWithCategories(id);
        contentRepository.deleteFromTableJointWithUsers(id);
        contentRepository.delete(song);
    }
}
