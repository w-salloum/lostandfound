package com.assignment.lostandfound.repository;

import com.assignment.lostandfound.entity.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LostItemRepository extends JpaRepository<LostItem, Long> {
    Optional<LostItem> findByItemIdAndPlaceId(Long itemId, Long placeId);
}