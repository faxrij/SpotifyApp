package com.example.spotifyproject.repository;

import com.example.spotifyproject.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends PagingAndSortingRepository<Category, String> {
    @Query(value = "select * " +
            "from category c " +
            "WHERE c.is_super_category=true ",
            nativeQuery = true)
    Category findSuperCategory();

    @Query(value = "select * " +
            "from category c " +
            "WHERE c.parent=:parentId ",
            nativeQuery = true)
    List<Category> findChildCategories(String parentId);

    @Query(value = "select * from category c where lower(c.name) like lower(:name) ", nativeQuery = true)
    Page<Category> findAllCategoriesLike(Pageable pageable, String name);

    @Modifying
    @Query(value = "DELETE FROM user_liked_category_table b WHERE b.category_id = :id",nativeQuery = true)
    void  deleteFromJointTableWithUserId(String id);

    @Modifying
    @Query(value = "DELETE FROM song_category_table s WHERE s.category_id = :id",nativeQuery = true)
    void  deleteFromJointTableWithSong(String id);
}