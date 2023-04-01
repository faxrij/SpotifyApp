package com.example.spotifyproject.model.response;


import com.example.spotifyproject.entity.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserResponse extends CommonResponseField{
    private String name;
    private String lastName;
    private String email;
    private Role role;
    @JsonIgnoreProperties(value = {"user_fk","invoices"})
    private List<ContractRecordResponse> contractRecords;
    @JsonIgnoreProperties(value = {"parent","song_list","users"})
    private List<CategoryResponse> followedCategories;
    @JsonIgnoreProperties(value = {"parent","categories","users"})
    private List<ContentResponse> followedSongs;

}
