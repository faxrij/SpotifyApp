package com.example.spotifyproject.controller;

import com.example.spotifyproject.model.response.CategoryResponse;
import com.example.spotifyproject.service.AuthenticationService;
import com.example.spotifyproject.service.UserCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserCategoryController {
    private final UserCategoryService userCategoryService;
    private final AuthenticationService authenticationService;

    @GetMapping("/{id}/category")
    public Page<CategoryResponse> getUserCategories(Pageable pageable,
                                                    @PathVariable String id) {
        return userCategoryService.getUserCategories(pageable, id, authenticationService.getAuthenticatedUserId());
    }

    @GetMapping("/{userId}/category/{categoryId}")
    public CategoryResponse getUserCategoryByContentId(@PathVariable String userId,
                                                       @PathVariable String categoryId) {
        return userCategoryService.getUserCategoryById(userId, categoryId, authenticationService.getAuthenticatedUserId());
    }

    @PostMapping("/{userId}/category/{categoryId}/follow")
    public void userLikeCategoryById(@PathVariable String userId,
                                 @PathVariable String categoryId) {
        userCategoryService.userLikeCategoryById(userId, categoryId, authenticationService.getAuthenticatedUserId());
    }

    @PostMapping("/{userId}/category/{categoryId}/unfollow")
    public void userRemoveLikedCategoryById(@PathVariable String userId,
                                        @PathVariable String categoryId) {
        userCategoryService.userRemoveLikedCategoryById(userId, categoryId, authenticationService.getAuthenticatedUserId());
    }

}
