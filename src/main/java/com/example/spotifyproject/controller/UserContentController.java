package com.example.spotifyproject.controller;

import com.example.spotifyproject.model.response.ContentResponse;
import com.example.spotifyproject.service.AuthenticationService;
import com.example.spotifyproject.service.UserContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserContentController {
    private final UserContentService userContentService;
    private final AuthenticationService authenticationService;

    @GetMapping("/{id}/content")
    public Page<ContentResponse> getUserContents(Pageable pageable,
                                                 @PathVariable String id) {
        return userContentService.getUserContents(pageable, id, authenticationService.getAuthenticatedUserId());
    }

    @GetMapping("/{userId}/content/{contentId}")
    public ContentResponse getUserContentsByContentId(@PathVariable String userId,
                                                      @PathVariable String contentId) {
        return userContentService.getUserContentsByContentId(userId, contentId, authenticationService.getAuthenticatedUserId());
    }

    @PostMapping("/{userId}/content/{contentId}/favorite")
    public void userLikeSongById(@PathVariable String userId,
                                 @PathVariable String contentId) {
        userContentService.userLikeSongById(userId, contentId, authenticationService.getAuthenticatedUserId());
    }

    @PostMapping("/{userId}/content/{contentId}/unfavorite")
    public void userRemoveLikedSongById(@PathVariable String userId,
                                        @PathVariable String contentId) {
        userContentService.userRemoveLikedSongById(userId, contentId, authenticationService.getAuthenticatedUserId());
    }

}
