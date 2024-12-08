package com.assignment.lostandfound.repository;

import com.assignment.lostandfound.entity.ClaimedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimedItemRepository extends JpaRepository<ClaimedItem, Long> {
    List<ClaimedItem> findByLostItemId(long lostItemId);
}