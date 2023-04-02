package com.example.spotifyproject.service;

import com.example.spotifyproject.entity.Category;
import com.example.spotifyproject.entity.Role;
import com.example.spotifyproject.entity.User;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.exception.ErrorCode;
import com.example.spotifyproject.model.request.category.CreateCategoryRequest;
import com.example.spotifyproject.model.request.category.UpdateCategoryRequest;
import com.example.spotifyproject.model.response.CategoryResponse;
import com.example.spotifyproject.repository.CategoryRepository;
import com.example.spotifyproject.repository.UserRepository;
import com.example.spotifyproject.service.mapper.FromCategoryToCategoryResponse;
import com.example.spotifyproject.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final FromCategoryToCategoryResponse fromCategoryToCategoryResponse;

    @Cacheable(value = "categoriesCache")
    public Page<CategoryResponse> getCategories(Pageable pageable, String name, String id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "Account does not exist")
        );

        if (!(user.getRole().equals(Role.MEMBER) || user.getRole().equals(Role.ADMIN))) {
            throw new BusinessException(ErrorCode.unauthorized, "User is not authenticated");
        }

        Page<Category> categories = categoryRepository.findAllCategoriesLike(pageable, "%".concat(name).concat("%"));

        return categories.map(fromCategoryToCategoryResponse::fromCategoryToCategoryResponse);
    }

    @Cacheable(value = "categoryCache", key = "#id")
    public CategoryResponse getCategoryById(String id, String userId) {

        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "Account does not exist")
        );

        if (!(user.getRole().equals(Role.MEMBER) || user.getRole().equals(Role.ADMIN))) {
            throw new BusinessException(ErrorCode.unauthorized, "User is not authenticated");
        }

        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "Category with provided id does not exist")
        );

        return fromCategoryToCategoryResponse.fromCategoryToCategoryResponse(category);
    }

    public void addCategory(CreateCategoryRequest request, String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "Account does not exist")
        );

        if (!user.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.unauthorized, "User is not authenticated");
        }

        Category superCategory = categoryRepository.findSuperCategory();

        categorySetter(request, superCategory);

    }

    private void categorySetter(CreateCategoryRequest request, Category superCategory) {
        Category newCategory = new Category();
        newCategory.setSuperCategory(false);
        newCategory.setName(request.getName());
        newCategory.setParent(superCategory);
        newCategory.setCreatedDate(DateUtil.now());
        newCategory.setModifiedDate(DateUtil.now());

        categoryRepository.save(newCategory);
    }

    public void addCategoryByParentId(String parentId, CreateCategoryRequest request, String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "Account does not exist")
        );

        if(!user.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.unauthorized, "User is not authenticated");
        }

        Category parentCategory = categoryRepository.findById(parentId).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "Parent Category does not exist")
        );

        categorySetter(request, parentCategory);
    }

    public void updateCategory(String id, UpdateCategoryRequest request, String userId) {
        Category category = checker(id, userId);

        Category parentCategory = categoryRepository.findById(request.getParentId()).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "Provided Parent Category does not exist")
        );

        category.setModifiedDate(DateUtil.now());
        category.setName(request.getName());
        category.setParent(parentCategory);

        categoryRepository.save(category);
    }

    public void deleteCategory(String id, String userId) {
        Category category = checker(id, userId);
        List<Category> childCategories = categoryRepository.findChildCategories(category.getId());
        Category parentCategory = categoryRepository.findSuperCategory();

        for (Category temp:childCategories) {
            temp.setParent(parentCategory);
            categoryRepository.save(temp);
        }

        categoryRepository.deleteFromJointTableWithUserId(id);
        categoryRepository.deleteFromJointTableWithSong(id);
        categoryRepository.delete(category);
    }

    private Category checker(String id, String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "Account does not exist")
        );

        if (!user.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.unauthorized, "User is not authenticated");
        }

        return categoryRepository.findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "Category does not exist")
        );
    }
}
