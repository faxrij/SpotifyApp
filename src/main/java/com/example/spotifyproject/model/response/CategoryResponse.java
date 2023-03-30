package com.example.spotifyproject.model.response;

import com.example.spotifyproject.entity.Category;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CategoryResponse extends CommonResponseField{
    private String id;
    private String name;
    @JsonIgnoreProperties(value = {"parent","song_list","users"})
    private Category parent;
}
