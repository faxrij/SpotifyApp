package com.example.spotifyproject.model.request.category;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UpdateCategoryRequest {
    @NotEmpty
    private String name;
    @NotEmpty
    private String parentId;
}
