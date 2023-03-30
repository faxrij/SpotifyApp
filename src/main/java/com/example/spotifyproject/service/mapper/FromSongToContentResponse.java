package com.example.spotifyproject.service.mapper;

import com.example.spotifyproject.entity.Song;
import com.example.spotifyproject.model.response.ContentResponse;
import org.springframework.stereotype.Service;


import static com.example.spotifyproject.service.mapper.HelperToGetParentCategoriesOfACategory.getAllParentCategories;

@Service
public class FromSongToContentResponse {

    public ContentResponse fromSongToContentResponse(Song temp) {
        ContentResponse contentResponse = new ContentResponse();

        contentResponse.setId(temp.getId());
        contentResponse.setName(temp.getName());
        contentResponse.setTitle(temp.getTitle());
        contentResponse.setLyrics(temp.getLyrics());
        contentResponse.setComposerName(temp.getComposerName());
        contentResponse.setCreatedDate(temp.getCreatedDate());
        contentResponse.setModifiedDate(temp.getModifiedDate());
        contentResponse.setCategoryNames(getAllParentCategories(temp.getCategories()));

        return contentResponse;
    }


}
