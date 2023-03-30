package com.example.spotifyproject.repository;

import com.example.spotifyproject.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends PagingAndSortingRepository<Subscription, String> {
    @Query(value = "select * " +
            "from subscription s " +
            "order by s.duration",
            nativeQuery = true)
    Page<Subscription> findAll(Pageable pageable);
}
