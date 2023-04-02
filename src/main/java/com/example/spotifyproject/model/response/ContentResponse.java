package com.example.spotifyproject.model.response;

import com.example.spotifyproject.entity.Category;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class ContentResponse extends CommonResponseField {
    private String name;
    private String lyrics;
    private String title;
    private String composerName;
    @JsonIgnoreProperties(value = {"parent","song_list","users"})
    private Set<Category> categoryNames;

}
