package com.example.spotifyproject.controller;

import com.example.spotifyproject.model.request.category.CreateCategoryRequest;
import com.example.spotifyproject.model.request.category.UpdateCategoryRequest;
import com.example.spotifyproject.model.response.CategoryResponse;
import com.example.spotifyproject.service.AuthenticationService;
import com.example.spotifyproject.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final AuthenticationService authenticationService;

    @GetMapping
    public Page<CategoryResponse> getCategories(Pageable pageable,
                                                @RequestParam(defaultValue = "") String name) {
        return categoryService.getCategories(pageable, name, authenticationService.getAuthenticatedUserId());
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategoryById(@PathVariable String id) {
        return categoryService.getCategoryById(id, authenticationService.getAuthenticatedUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addCategoryByParentId(@Valid @RequestBody CreateCategoryRequest createCategoryRequest) {
        categoryService.addCategory(createCategoryRequest, authenticationService.getAuthenticatedUserId());
    }

    @PostMapping("/{parentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addCategoryByParentId(@PathVariable String parentId,
                            @Valid @RequestBody CreateCategoryRequest createCategoryRequest) {
        categoryService.addCategoryByParentId(parentId, createCategoryRequest, authenticationService.getAuthenticatedUserId());
    }

    @PutMapping("/{id}")
    public void updateCategory(@PathVariable String id,
                               @Valid @RequestBody UpdateCategoryRequest updateCategoryRequest) {
        categoryService.updateCategory(id, updateCategoryRequest, authenticationService.getAuthenticatedUserId());
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id, authenticationService.getAuthenticatedUserId());
    }
}
