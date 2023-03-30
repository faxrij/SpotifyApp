package com.example.spotifyproject.repository;

import com.example.spotifyproject.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User,String> {

    Optional<User> findUserByEmail(String email);

    @Query(value = "select * " +
            "from users u " +
            "order by u.id",
            nativeQuery = true)
    Page<User> findAll(Pageable pageable);

    @Query(value = "select song_id " +
            "from user_liked_song_table u where u.user_id=:userId ",
            nativeQuery = true)
    Page<String> findSongsByUserId(Pageable pageable, String userId);

    @Query(value = "select * " +
            "from user_liked_song_table u where u.user_id=:userId and u.song_id=:contentId",
            nativeQuery = true)
    List<String> findSongsByUserIdAndBySongId(String userId, String contentId);

    @Query(value = "select category_id " +
            "from user_liked_category_table u where u.user_id=:userId ",
            nativeQuery = true)
    Page<String> findCategoriesByUserId(Pageable pageable, String userId);

    @Query(value = "select * " +
            "from user_liked_category_table u where u.user_id=:userId and u.category_id=:categoryId",
            nativeQuery = true)
    List<String> findCategoriesByUserIdAndByCategoryId(String userId, String categoryId);

    @Modifying
    @Query(value = "insert into  " +
            "user_liked_song_table values (?1,?2)",
            nativeQuery = true)
    void likeSongByUserIdAndSongId(String userId, String songId);

    @Modifying
    @Query(value = "DELETE FROM user_liked_song_table s WHERE s.user_id = :userId and s.song_id=:songId",nativeQuery = true)
    void  removeLikedSongByUserIdAndSongId(String userId, String songId);

    @Modifying
    @Query(value = "insert into  " +
            "user_liked_category_table values (?1,?2)",
            nativeQuery = true)
    void likeCategoryByUserIdAndSongId(String userId, String categoryId);

    @Modifying
    @Query(value = "DELETE FROM user_liked_category_table s WHERE s.user_id = :userId and s.category_id=:categoryId",nativeQuery = true)
    void  removeLikedCategoryByUserIdAndSongId(String userId, String categoryId);
}
