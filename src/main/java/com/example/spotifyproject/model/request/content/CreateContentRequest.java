package com.example.spotifyproject.model.request.content;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
public class CreateContentRequest {
    @NotEmpty
    private String name;
    @NotEmpty
    private String title;
    @NotEmpty
    @Length(min = 8, message = "Provided lyrics should be at least 8 characters")
    private String lyrics;
    @NotEmpty
    private String composer;
}
