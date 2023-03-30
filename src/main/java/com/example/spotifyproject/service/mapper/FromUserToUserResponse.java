package com.example.spotifyproject.service.mapper;

import com.example.spotifyproject.entity.Category;
import com.example.spotifyproject.entity.ContractRecord;
import com.example.spotifyproject.entity.Song;
import com.example.spotifyproject.entity.User;
import com.example.spotifyproject.model.response.CategoryResponse;
import com.example.spotifyproject.model.response.ContentResponse;
import com.example.spotifyproject.model.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class FromUserToUserResponse {

    private final FromCategoryToCategoryResponse fromCategoryToCategoryResponse;
    private final FromSongToContentResponse fromSongToContentResponse;

    public UserResponse fromUserToUserResponse(User temp) {

        UserResponse userResponse = new UserResponse();
        List<Category> categories = temp.getCategories();
        List<Song> songs = temp.getSongs();
        List<ContractRecord> contractRecords = temp.getContractRecords();

        List<CategoryResponse> categoryResponses = new ArrayList<>();
        List<ContentResponse> contentResponses = new ArrayList<>();

        for (Category category:categories) {
            categoryResponses.add(fromCategoryToCategoryResponse.fromCategoryToCategoryResponse(category));
        }

        for (Song song:songs) {
            contentResponses.add(fromSongToContentResponse.fromSongToContentResponse(song));
        }


        userResponse.setId(temp.getId());
        userResponse.setName(temp.getName());
        userResponse.setEmail(temp.getEmail());
        userResponse.setRole(temp.getRole());
        userResponse.setLastName(temp.getLastName());
        userResponse.setCreatedDate(temp.getCreatedDate());
        userResponse.setModifiedDate(temp.getModifiedDate());
        userResponse.setFollowedCategories(categoryResponses);
        userResponse.setFollowedSongs(contentResponses);
        userResponse.setContractRecords(contractRecords);
        return userResponse;
    }

}
