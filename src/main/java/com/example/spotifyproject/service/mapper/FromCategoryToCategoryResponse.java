package com.example.spotifyproject.service.mapper;

import com.example.spotifyproject.entity.Category;
import com.example.spotifyproject.model.response.CategoryResponse;
import org.springframework.stereotype.Service;

@Service
public class FromCategoryToCategoryResponse {
    public CategoryResponse fromCategoryToCategoryResponse(Category temp) {
        CategoryResponse categoryResponse = new CategoryResponse();

        categoryResponse.setId(temp.getId());
        categoryResponse.setName(temp.getName());
        categoryResponse.setCreatedDate(temp.getCreatedDate());
        categoryResponse.setModifiedDate(temp.getModifiedDate());
        categoryResponse.setParent(temp.getParent());

        return categoryResponse;
    }
}
