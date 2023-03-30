package com.example.spotifyproject.service;

import com.example.spotifyproject.entity.Category;
import com.example.spotifyproject.entity.User;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.exception.ErrorCode;
import com.example.spotifyproject.model.response.CategoryResponse;
import com.example.spotifyproject.repository.CategoryRepository;
import com.example.spotifyproject.repository.UserRepository;
import com.example.spotifyproject.service.mapper.FromCategoryToCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCategoryService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FromCategoryToCategoryResponse fromCategoryToCategoryResponse;

    public Page<CategoryResponse> getUserCategories(Pageable pageable, String id, String userId) {

        if (!(id.equals(userId))) {
            throw new BusinessException(ErrorCode.forbidden, "You cannot see other users' content");
        }

        userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "Account does not exist")
        );

        Page<String> categoryIds = userRepository.findCategoriesByUserId(pageable, id);
        List<Category> categoryList = new ArrayList<>();

        for (String temp: categoryIds) {
            Category category = categoryRepository.findById(temp).orElseThrow(
                    () -> new BusinessException(ErrorCode.internal_server_error, "Server Error")
            );
            categoryList.add(category);
        }
        return new PageImpl<>(categoryList.stream().map(fromCategoryToCategoryResponse::fromCategoryToCategoryResponse).collect(Collectors.toList()));

    }

    public CategoryResponse getUserCategoryByContentId(String userId, String categoryId, String currentUserId) {
        if (!(currentUserId.equals(userId))) {
            throw new BusinessException(ErrorCode.forbidden, "You cannot see other users' content");
        }

        userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "Account does not exist")
        );

        List<String> containingRow = userRepository.findCategoriesByUserIdAndByCategoryId(userId, categoryId);

        if (containingRow.isEmpty()) {
            throw new BusinessException(ErrorCode.resource_missing, "User has not liked such a song");
        }

        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new BusinessException(ErrorCode.internal_server_error, "Error")
        );

        return fromCategoryToCategoryResponse.fromCategoryToCategoryResponse(category);
    }

    public void userLikeCategoryById(String userId, String contentId, String currentUserId) {
        if (!currentUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.forbidden, "You are not allowed here");
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "User is not found")
        );
        Category category = categoryRepository.findById(contentId).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "Category does not exist")
        );

        if (user.getCategories().contains(category)) {
            throw new BusinessException(ErrorCode.song_is_already_liked, "This category is already liked by user");
        }

        userRepository.likeCategoryByUserIdAndSongId(userId,contentId);
    }

    public void userRemoveLikedCategoryById(String userId, String contentId, String currentUserId) {
        if (!currentUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.forbidden, "You are not allowed here");
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "User is not found")
        );
        Category category = categoryRepository.findById(contentId).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "Category does not exist")
        );

        if (!user.getCategories().contains(category)) {
            throw new BusinessException(ErrorCode.song_is_already_liked, "This category is not liked by user");
        }

        userRepository.removeLikedCategoryByUserIdAndSongId(userId,contentId);
    }

}
