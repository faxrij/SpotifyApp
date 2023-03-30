package com.example.spotifyproject.service;

import com.example.spotifyproject.entity.Song;
import com.example.spotifyproject.entity.User;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.exception.ErrorCode;
import com.example.spotifyproject.model.response.ContentResponse;
import com.example.spotifyproject.repository.ContentRepository;
import com.example.spotifyproject.repository.UserRepository;
import com.example.spotifyproject.service.mapper.FromSongToContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserContentService {
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final FromSongToContentResponse fromSongToContentResponse;

    public Page<ContentResponse> getUserContents(Pageable pageable, String id, String userId) {

        if (!(id.equals(userId))) {
            throw new BusinessException(ErrorCode.forbidden, "You cannot see other users' content");
        }

        userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "User is not found")
        );

        Page<String> songIds = userRepository.findSongsByUserId(pageable, id);
        List<ContentResponse> songList = new ArrayList<>();

        for (String temp: songIds) {
            Song song = contentRepository.findById(temp).orElseThrow(() -> new BusinessException(ErrorCode.internal_server_error, "Server Error"));
            songList.add(fromSongToContentResponse.fromSongToContentResponse(song));
        }

        return new PageImpl<>(songList);
    }

    public ContentResponse getUserContentsByContentId(String userId, String contentId, String currentUserId) {

        if (!(currentUserId.equals(userId))) {
            throw new BusinessException(ErrorCode.forbidden, "You cannot see other users' content");
        }

        userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "User is not found")
        );

        List<String> containingRow = userRepository.findSongsByUserIdAndBySongId(userId, contentId);

        if (containingRow.isEmpty()) {
            throw new BusinessException(ErrorCode.resource_missing, "User has not liked such a song");
        }

        Song song = contentRepository.findById(contentId).orElseThrow(
                () -> new BusinessException(ErrorCode.internal_server_error, "Error")
        );

        return fromSongToContentResponse.fromSongToContentResponse(song);

    }

    public void userLikeSongById(String userId, String contentId, String currentUserId) {
        User user = getUser(userId, currentUserId);
        Song song = contentRepository.findById(contentId).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "Song does not exist")
        );

        if (user.getSongs().contains(song)) {
            throw new BusinessException(ErrorCode.song_is_already_liked, "This song is already liked by user");
        }

        userRepository.likeSongByUserIdAndSongId(userId,contentId);

    }

    public void userRemoveLikedSongById(String userId, String contentId, String currentUserId) {
        User user = getUser(userId, currentUserId);
        Song song = contentRepository.findById(contentId).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "Song does not exist")
        );

        if (!user.getSongs().contains(song)) {
            throw new BusinessException(ErrorCode.song_is_already_liked, "This song is not liked by user");
        }

        userRepository.removeLikedSongByUserIdAndSongId(userId,contentId);
    }

    private User getUser(String userId, String currentUserId) {
        if (!currentUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.forbidden, "You are not allowed here");
        }

        return userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "User is not found")
        );
    }

}
