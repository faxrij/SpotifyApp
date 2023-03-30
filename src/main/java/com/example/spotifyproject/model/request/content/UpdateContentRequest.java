package com.example.spotifyproject.model.request.content;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UpdateContentRequest {
    @NotEmpty
    private String name;
    @NotEmpty
    private String title;
    @NotEmpty
    private String lyric;
    @NotEmpty
    private String composerName;
}
