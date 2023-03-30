package com.example.spotifyproject.repository;

import com.example.spotifyproject.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends PagingAndSortingRepository<Song, String > {
    @Query(value = "select * " +
            "from song s " +
            "order by s.id",
            nativeQuery = true)
    Page<Song> findAll(Pageable pageable);

    @Query(value = "select * from song s where lower(s.name) like lower(:name) " +
            "and lower(s.lyrics) like lower(:lyrics) " +
            "and lower(s.title) like  lower(:title)" +
            "and lower(s.composer) like lower(:composerName)", nativeQuery = true)
    Page<Song> findAllContentLike(Pageable pageable, String name, String lyrics, String title, String composerName);

    @Modifying
    @Query(value = "DELETE FROM song_category_table s WHERE s.song_id = :id",nativeQuery = true)
    void deleteFromTableJointWithCategories(String id);

    @Modifying
    @Query(value = "DELETE FROM user_liked_song_table s WHERE s.song_id = :id",nativeQuery = true)
    void deleteFromTableJointWithUsers(String id);
}
