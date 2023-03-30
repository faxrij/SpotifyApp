package com.example.spotifyproject.service.mapper;

import com.example.spotifyproject.entity.Category;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class HelperToGetParentCategoriesOfACategory {
    public static Set<Category> getAllParentCategories(List<Category> categories) {
        Set<Category> listOfParents = new HashSet<>();

        for (Category temp: categories) {
            while (temp.getParent()!=null) {
                Category parentCategory = temp.getParent();
                listOfParents.add(parentCategory);
                temp = temp.getParent();
            }
        }
        return listOfParents;
    }
}
