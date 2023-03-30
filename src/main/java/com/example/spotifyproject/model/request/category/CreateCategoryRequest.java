package com.example.spotifyproject.model.request.category;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CreateCategoryRequest {
    @NotEmpty
    private String name;
}
