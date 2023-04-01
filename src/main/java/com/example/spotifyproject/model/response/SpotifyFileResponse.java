package com.example.spotifyproject.model.response;

import com.example.spotifyproject.entity.File;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class SpotifyFileResponse {
    private String id;
    private String url;
    private String name;
    private String contentType;


    public static SpotifyFileResponse fromEntity(File file) {
        if (file == null) {
            return null;
        }

        return SpotifyFileResponse.builder()
                .id(file.getId())
                .url(file.getUrl())
                .name(file.getName())
                .contentType(file.getContentType())
                .build();
    }
}
