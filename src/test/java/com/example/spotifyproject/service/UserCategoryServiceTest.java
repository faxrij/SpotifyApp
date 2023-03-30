package com.example.spotifyproject.service;

import com.example.spotifyproject.entity.Category;
import com.example.spotifyproject.entity.Role;
import com.example.spotifyproject.entity.User;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.model.response.CategoryResponse;
import com.example.spotifyproject.repository.CategoryRepository;
import com.example.spotifyproject.repository.UserRepository;
import com.example.spotifyproject.service.mapper.FromCategoryToCategoryResponse;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserCategoryServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FromCategoryToCategoryResponse fromCategoryToCategoryResponse;

    @InjectMocks
    private UserCategoryService userCategoryService;


    @Test
    public void testGetUserCategories() {
        // given
        String id = "1";
        String userId = "1";
        Category category1 = new Category();
        category1.setName("Category 1");
        category1.setId("1");
        Category category2 = new Category();
        category2.setId("2");
        category2.setName("Category 2");
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add(category1.getId());
        categoryIds.add(category2.getId());

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        when(userRepository.findCategoriesByUserId(any(Pageable.class), eq("1"))).thenReturn(new PageImpl<>(categoryIds));

        when(categoryRepository.findById("1")).thenReturn(Optional.of(category1));
        when(categoryRepository.findById("2")).thenReturn(Optional.of(category2));

        when(fromCategoryToCategoryResponse.fromCategoryToCategoryResponse(category1)).thenReturn(new CategoryResponse());
        when(fromCategoryToCategoryResponse.fromCategoryToCategoryResponse(category2)).thenReturn(new CategoryResponse());

        Page<CategoryResponse> result = userCategoryService.getUserCategories(Pageable.unpaged(), id, userId);

        assertEquals(2, result.getTotalElements());
        Mockito.verify(userRepository, times(1)).findById(userId);
        Mockito.verify(userRepository, times(1)).findCategoriesByUserId(any(Pageable.class), eq(id));
        Mockito.verify(categoryRepository, times(1)).findById("1");
        Mockito.verify(categoryRepository, times(1)).findById("2");
        Mockito.verify(fromCategoryToCategoryResponse, times(1)).fromCategoryToCategoryResponse(category1);
        verify(fromCategoryToCategoryResponse, times(1)).fromCategoryToCategoryResponse(category2);
    }

    @Test
    public void testGetUserCategoriesWithInvalidUserId() {
        // given
        String id = "1";
        String userId = "2";

        // when + then
        BusinessException exception = assertThrows(BusinessException.class, () -> userCategoryService.getUserCategories(Pageable.unpaged(), id, userId));
        assertEquals("forbidden", exception.getErrorCode());
    }

    @Test
    public void testGetUserCategoriesWithUnauthorizedUser() {
        // given
        String id = "1";
        String userId = "1";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> userCategoryService.getUserCategories(Pageable.unpaged(), id, userId));
        assertEquals("account_missing", exception.getErrorCode());
    }

    @Test
    public void testGetUserCategoryByContentId_Success() {
        String id = "1";
        String userId = "1";
        Category category1 = new Category();
        category1.setName("Category 1");
        category1.setId("1");

        List<String> categoryIds = new ArrayList<>();
        categoryIds.add(category1.getId());

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(userRepository.findCategoriesByUserIdAndByCategoryId(userId, category1.getId())).thenReturn(categoryIds);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category1));
        when(fromCategoryToCategoryResponse.fromCategoryToCategoryResponse(category1)).thenReturn(new CategoryResponse());

        CategoryResponse result = userCategoryService.getUserCategoryByContentId(userId, id, userId);

        assertNotNull(result);
    }

    @Test
    public void testGetUserCategoryByContentId_UserNotFound() {
        when(userRepository.findById(any(String.class))).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> userCategoryService.getUserCategoryByContentId("1", "1", "1"));
    }

    @Test
    public void testGetUserCategoryByContentId_UserNotAllowed() {
        assertThrows(BusinessException.class, () -> userCategoryService.getUserCategoryByContentId("1", "1", "2"));
    }

    @Test
    public void testGetUserCategoryByContentId_CategoryNotFound() {
        when(userRepository.findById("1")).thenReturn(Optional.of(new User()));

        assertThrows(BusinessException.class, () -> userCategoryService.getUserCategoryByContentId("1", "cat", "1"));
    }

    @Test
    void testUserLikeCategoryById() {
        // given
        User user = new User();
        user.setId("1");
        user.setRole(Role.ADMIN);
        user.setEmail("user@gmail.com");

        user.setCategories(new ArrayList<>());
        Category category = new Category();
        category.setId("3");
        category.setName("Category");
        category.setModifiedDate(DateUtil.now());
        category.setCreatedDate(DateUtil.now());

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(categoryRepository.findById("3")).thenReturn(Optional.of(category));

        // when
        userCategoryService.userLikeCategoryById("1", "3", "1");

        // then
        verify(userRepository).likeCategoryByUserIdAndSongId("1", "3");
    }

    @Test
    void testUserLikeCategoryByIdWhenNoMatch() {
        assertThrows(BusinessException.class, () -> userCategoryService.userLikeCategoryById("1", "3", "2"));
    }

    @Test
    void testUserLikeCategoryByIdWhenCategoryDoesNotExist() {
        when(userRepository.findById("1")).thenReturn(Optional.of(new User()));
        when(categoryRepository.findById(anyString())).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> userCategoryService.userLikeCategoryById("1", "3", "1"));
    }

    @Test
    void testUserLikeCategoryByIdWhenCategoryIsAlreadyLiked() {
        // given
        User user = new User();
        user.setId("1");
        user.setRole(Role.ADMIN);
        user.setEmail("user@gmail.com");

        Category category = new Category();
        category.setId("3");
        category.setName("Category");
        category.setModifiedDate(DateUtil.now());
        category.setCreatedDate(DateUtil.now());
        List<Category> categories = new ArrayList<>();
        categories.add(category);
        user.setCategories(categories);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(categoryRepository.findById("3")).thenReturn(Optional.of(category));

        // when + then
        assertThrows(BusinessException.class, () -> userCategoryService.userLikeCategoryById("1", "3", "1"));
    }

    @Test
    void testUserRemoveLikedCategoryByIdWhenNoMatch() {
        assertThrows(BusinessException.class, () -> userCategoryService.userRemoveLikedCategoryById("1", "3", "2"));
    }

    @Test
    void testUserRemoveLikedCategoryByIdWhenUserDoesNotExist() {
        when(userRepository.findById("1")).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> userCategoryService.userRemoveLikedCategoryById("1", "3", "1"));
    }

    @Test
    void testUserRemoveLikedCategoryByIdWhenCategoryExists() {
        // given
        User user = new User();
        user.setId("1");
        Category category = new Category();
        category.setId("3");
        category.setName("Category");
        category.setModifiedDate(DateUtil.now());
        category.setCreatedDate(DateUtil.now());

        List<Category> categories = new ArrayList<>();
        user.setCategories(categories);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(categoryRepository.findById(anyString())).thenReturn(Optional.of(category));
        assertThrows(BusinessException.class, () -> userCategoryService.userRemoveLikedCategoryById("1", "3", "1"));
    }

    @Test
    void testUserRemoveLikedCategoryById() {
        // given
        User user = new User();
        user.setId("1");
        user.setEmail("testUser@gmail.com");
        user.setRole(Role.MEMBER);
        user.setVerified(true);

        user.setCategories(new ArrayList<>());
        Category category = new Category();
        category.setId("1");
        category.setName("testCategory");

        user.getCategories().add(category);

        // when
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(categoryRepository.findById("1")).thenReturn(Optional.of(category));

        userCategoryService.userRemoveLikedCategoryById("1", "1", "1");
        verify(userRepository).removeLikedCategoryByUserIdAndSongId("1", "1");
    }


    @Test
    void testUserRemoveLikedCategoryByIdWhenCategoryDoesNotExist() {
        // given
        User user = new User();
        user.setId("1");
        user.setEmail("testUser@gmail.com");
        user.setRole(Role.MEMBER);
        user.setVerified(true);

        Category category = new Category();
        category.setId("1");
        category.setName("testCategory");
        List<Category> categories = new ArrayList<>();
        categories.add(category);
        user.setCategories(categories);
        user.getCategories().add(category);

        // when
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(categoryRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> userCategoryService.userRemoveLikedCategoryById("1", "1", "1"));
    }

}
