package com.example.spotifyproject.controller;

import com.example.spotifyproject.model.request.content.CreateContentRequest;
import com.example.spotifyproject.model.request.content.UpdateContentRequest;
import com.example.spotifyproject.model.response.ContentResponse;
import com.example.spotifyproject.service.AuthenticationService;
import com.example.spotifyproject.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/content")
@RequiredArgsConstructor
public class ContentController {
    private final ContentService contentService;
    private final AuthenticationService authenticationService;

    @GetMapping
    public Page<ContentResponse> getAllContent(Pageable pageable,
                                               @RequestParam(defaultValue = "") String name,
                                               @RequestParam(defaultValue = "") String lyrics,
                                               @RequestParam(defaultValue = "") String title,
                                               @RequestParam(defaultValue = "") String composerName) {
        return contentService.getAllContent(pageable, name, lyrics, title, composerName, authenticationService.getAuthenticatedUserId());
    }

    @GetMapping("/{id}")
    public ContentResponse getContentById(@PathVariable String id) {
        return contentService.getContentById(id,authenticationService.getAuthenticatedUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addContent(@Valid @RequestBody CreateContentRequest createContentRequest) {
        contentService.addContent(createContentRequest, authenticationService.getAuthenticatedUserId());
    }

    @PutMapping("/{id}")
    public void updateContent(@PathVariable String id,
            @Valid @RequestBody UpdateContentRequest updateContentRequest) {
        contentService.updateContent(id, updateContentRequest, authenticationService.getAuthenticatedUserId());
    }

    @DeleteMapping("/{id}")
    public void deleteContent(@PathVariable String id) {
        contentService.deleteContent(id, authenticationService.getAuthenticatedUserId());
    }
}
