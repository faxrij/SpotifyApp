package com.example.spotifyproject.service;

import com.example.spotifyproject.entity.*;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.model.request.category.CreateCategoryRequest;
import com.example.spotifyproject.model.request.category.UpdateCategoryRequest;
import com.example.spotifyproject.model.response.CategoryResponse;
import com.example.spotifyproject.repository.CategoryRepository;
import com.example.spotifyproject.repository.UserRepository;
import com.example.spotifyproject.service.mapper.FromCategoryToCategoryResponse;
import com.example.spotifyproject.util.DateUtil;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private FromCategoryToCategoryResponse fromCategoryToCategoryResponse;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId("1");
        user.setRole(Role.MEMBER);
    }

    @Test
    public void testGetCategories() {
        // given
       when(userRepository.findById("123")).thenReturn(Optional.of(user));

        Category category = new Category();
        category.setId("123");
        category.setName("Test Category");
        category.setModifiedDate(DateUtil.now());
        category.setCreatedDate(DateUtil.now());

        CategoryResponse categoryResponse = new CategoryResponse();

        categoryResponse.setParent(category.getParent());
        categoryResponse.setName(category.getName());
        categoryResponse.setCreatedDate(category.getCreatedDate());
        categoryResponse.setModifiedDate(category.getModifiedDate());
        categoryResponse.setId(category.getId());

        Page<Category> categoryPage = new PageImpl<>(Collections.singletonList(category));

        when(categoryRepository.findAllCategoriesLike(Pageable.unpaged(), "%%")).thenReturn(categoryPage);

        when(fromCategoryToCategoryResponse.fromCategoryToCategoryResponse(any(Category.class))).thenReturn(categoryResponse);


        Page<CategoryResponse> result = categoryService.getCategories(Pageable.unpaged(),"","123");

        // then
        assertFalse(result.getContent().isEmpty());
        assertEquals(result.getContent().get(0).getId(), category.getId());
        assertEquals(result.getContent().get(0).getName(), category.getName());
        assertEquals(result.getContent().get(0).getParent(), category.getParent());
        assertEquals(result.getContent().get(0).getCreatedDate(), category.getCreatedDate());
        assertEquals(result.getContent().get(0).getModifiedDate(), category.getModifiedDate());
    }

    @Test
    public void testGetCategoriesWithInvalidUserId() {
        // given
        when(userRepository.findById("2")).thenReturn(Optional.empty());

        // then
        assertThrows(BusinessException.class, () -> categoryService.getCategories(Pageable.unpaged(), "","2"));
    }

    @Test
    public void testGetCategoriesWithUnauthorizedUser() {
        // given
        user.setRole(Role.GUEST);

        // when
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        // then
        assertThrows(BusinessException.class, () -> categoryService.getCategories(Pageable.unpaged(),"", "1"));
    }


    @Test
    public void testGetCategoryById_Success() {
        // given
        String categoryId = "1";
        String userId = "1";
        Song song = new Song();
        song.setId("1");
        user.setId(userId);
        user.setRole(Role.ADMIN);
        Category category = new Category();
        category.setId(categoryId);
        category.setName("PERFECT");
        category.setCreatedDate(DateUtil.now());
        category.setModifiedDate(DateUtil.now());

        CategoryResponse expected = new CategoryResponse();
        expected.setId(category.getId());
        expected.setName("PERFECT");
        expected.setModifiedDate(category.getModifiedDate());
        expected.setCreatedDate(category.getCreatedDate());

        category.setSong_list(Collections.singletonList(song));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(fromCategoryToCategoryResponse.fromCategoryToCategoryResponse(Mockito.any())).thenReturn(expected);

        // when
        CategoryResponse response = categoryService.getCategoryById(categoryId, userId);

        // then
        assertNotNull(response);
        assertEquals(category.getName(), response.getName());
    }

    @Test
    public void testGetCategoryById_UserNotFound() {
        // given
        String categoryId = "1";
        String userId = "1";

        when(userRepository.findById("1")).thenReturn(Optional.empty());

        // when + then
        assertThrows(BusinessException.class, () -> categoryService.getCategoryById(categoryId, userId));
    }

    @Test
    public void testGetCategoryById_UnauthorizedUser() {
        // given
        String categoryId = "1";
        String userId = "1";
        user.setId(userId);
        user.setRole(Role.INACTIVE_MEMBER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when + then
        assertThrows(BusinessException.class, () -> categoryService.getCategoryById(categoryId, userId));
    }

    @Test
    public void testGetCategoryById_CategoryNotFound() {
        // given
        String categoryId = "1";
        String userId = "1";
        user.setId(userId);
        user.setRole(Role.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when + then
        assertThrows(BusinessException.class, () -> categoryService.getCategoryById(categoryId, userId));
    }

    @Test
    public void testAddCategorySuccessfully() {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Test Category");

        user.setId("testUser");
        user.setRole(Role.ADMIN);

        Category superCategory = new Category();
        superCategory.setId("superCategoryId");
        superCategory.setSuperCategory(true);

        when(userRepository.findById("testUser")).thenReturn(Optional.of(user));
        when(categoryRepository.findSuperCategory()).thenReturn(superCategory);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);

        // when
        categoryService.addCategory(request, "testUser");

        // then
        verify(userRepository, times(1)).findById("testUser");
        verify(categoryRepository, times(1)).findSuperCategory();
        verify(categoryRepository, times(1)).save(categoryCaptor.capture());

        Category savedCategory = categoryCaptor.getValue();
        assertEquals("Test Category", savedCategory.getName());
        assertEquals(superCategory, savedCategory.getParent());
        assertFalse(savedCategory.isSuperCategory());
    }

    @Test
    public void testAddCategoryWithNonExistentUser() {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Test Category");

        when(userRepository.findById("testUser")).thenReturn(Optional.empty());

        // when and then
        assertThrows(BusinessException.class, () -> categoryService.addCategory(request, "testUser"));
        verify(categoryRepository, never()).findSuperCategory();
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    public void testAddCategoryWithNonAdminUser() {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Test Category");

        user.setId("testUser");
        user.setRole(Role.MEMBER);

        when(userRepository.findById("testUser")).thenReturn(Optional.of(user));

        // act and assert
        assertThrows(BusinessException.class, () -> categoryService.addCategory(request, "testUser"));
        verify(categoryRepository, never()).findSuperCategory();
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void testAddCategoryByParentId() {
        String parentId = "parent_id";
        String userId = "user_id";
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Category Name");

        User adminUser = new User();
        adminUser.setId(userId);
        adminUser.setRole(Role.ADMIN);

        Category parentCategory = new Category();
        parentCategory.setId(parentId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parentCategory));

        categoryService.addCategoryByParentId(parentId, request, userId);

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testAddCategoryByParentIdWithNonAdminUser() {
        String parentId = "parent_id";
        String userId = "user_id";
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Category Name");

        user.setId(userId);
        user.setRole(Role.MEMBER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> categoryService.addCategoryByParentId(parentId, request, userId));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void testAddCategoryByParentIdWithInvalidParentId() {
        String parentId = "invalid_parent_id";
        String userId = "user_id";
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Category Name");

        User adminUser = new User();
        adminUser.setId(userId);
        adminUser.setRole(Role.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));
        when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> categoryService.addCategoryByParentId(parentId, request, userId));

        assertEquals("resource_missing", exception.getErrorCode());
        assertEquals("Parent Category does not exist", exception.getMessage());

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void testAddCategoryByParentIdWithNonExistingUser() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Category Name");

        when(userRepository.findById("123")).thenReturn(Optional.empty());


        assertThrows(BusinessException.class, () -> categoryService.addCategoryByParentId("12", request, "123"));
    }

    @Test
    void testUpdateCategory_Success() {
        //given

        String id = "1";
        String parentId = "2";
        String userId = "3";
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("New Category Name");
        request.setParentId(parentId);

        User user = new User();
        user.setId(userId);
        user.setRole(Role.ADMIN);

        Category category = new Category();
        category.setId(id);
        category.setName("Category Name");
        category.setParent(new Category());

        Category parentCategory = new Category();
        parentCategory.setId(parentId);
        parentCategory.setName("Parent Category");

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parentCategory));


        categoryService.updateCategory(id, request, userId);

        // then
        verify(userRepository, times(1)).findById(userId);
        verify(categoryRepository, times(1)).findById(id);
        verify(categoryRepository, times(1)).findById(parentId);
        verify(categoryRepository, times(1)).save(category);

        assertEquals(request.getName(), category.getName());
        assertEquals(parentCategory, category.getParent());

    }

    @Test
    void testUpdateCategory_withNonExistingUser() {
        //given

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("New Category Name");
        request.setParentId("123");

        //when
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // then
        assertThrows(BusinessException.class, () -> categoryService.updateCategory("12", request, user.getId()));
    }

    @Test
    void testUpdateCategory_withUnauthorizedUser() {
        //given

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("New Category Name");
        request.setParentId("123");

        //when
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // then
        assertThrows(BusinessException.class, () -> categoryService.updateCategory("12", request, user.getId()));
    }

    @Test
    void testUpdateCategory_withNonExistingCategory() {
        //given
        user.setRole(Role.ADMIN);
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("New Category Name");
        request.setParentId("123");

        //when
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(categoryRepository.findById("12")).thenReturn(Optional.empty());

        // then
        assertThrows(BusinessException.class, () -> categoryService.updateCategory("12", request, user.getId()));
    }

    @Test
    void testUpdateCategory_withNonExistingParentCategory() {
        //given
        user.setRole(Role.ADMIN);
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("New Category Name");
        request.setParentId("123");

        Category category = new Category();
        category.setName("Category Test");

        //when
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(categoryRepository.findById("12")).thenReturn(Optional.of(category));
        when(categoryRepository.findById(request.getParentId())).thenReturn(Optional.empty());

        // then
        assertThrows(BusinessException.class, () -> categoryService.updateCategory("12", request, user.getId()));
    }

    @Test
    public void deleteCategory_success() {
        // given

        Category category = new Category();
        category.setId("1");
        category.setName("Test Category");

        user.setRole(Role.ADMIN);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(categoryRepository.findById("1")).thenReturn(Optional.of(category));
        when(categoryRepository.findChildCategories("1")).thenReturn(new ArrayList<>());
        when(categoryRepository.findSuperCategory()).thenReturn(category);

        // when
        categoryService.deleteCategory("1", "1");

        // then
        verify(categoryRepository, times(1)).deleteFromJointTableWithUserId("1");
        verify(categoryRepository, times(1)).delete(category);
        verify(categoryRepository, times(0)).save(any());
    }

    @Test
    public void deleteCategory_unauthorizedUser() {
        // given
        user.setRole(Role.MEMBER);
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        // when + then
        Assertions.assertThrows(BusinessException.class, () -> categoryService.deleteCategory("1", "1"));
        verify(categoryRepository, times(0)).deleteFromJointTableWithUserId(anyString());
        verify(categoryRepository, times(0)).delete(any());
        verify(categoryRepository, times(0)).save(any());
    }

    @Test
    public void deleteCategory_categoryNotFound() {
        // given
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(categoryRepository.findById("1")).thenReturn(Optional.empty());

        user.setRole(Role.ADMIN);

        // when + then
        Assertions.assertThrows(BusinessException.class, () -> categoryService.deleteCategory("1", "1"));
        verify(categoryRepository, times(0)).deleteFromJointTableWithUserId(anyString());
        verify(categoryRepository, times(0)).delete(any());
        verify(categoryRepository, times(0)).save(any());
    }

    @Test
    public void deleteCategory_withChildCategories() {
        // given
        Category category = new Category();
        category.setId("1");
        category.setName("Test Category");

        user.setRole(Role.ADMIN);

        List<Category> childCategories = new ArrayList<>();
        Category childCategory = new Category();
        childCategory.setId("2");
        childCategory.setName("Child Category");
        childCategory.setParent(category);
        childCategories.add(childCategory);

        // when
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(categoryRepository.findById("1")).thenReturn(Optional.of(category));
        when(categoryRepository.findChildCategories("1")).thenReturn(childCategories);
        when(categoryRepository.findSuperCategory()).thenReturn(category);

        categoryService.deleteCategory("1", "1");

        // then
        verify(categoryRepository, times(1)).deleteFromJointTableWithUserId("1");
        verify(categoryRepository, times(1)).delete(category);

    }
}