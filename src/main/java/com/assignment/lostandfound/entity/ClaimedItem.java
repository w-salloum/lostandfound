package com.assignment.lostandfound.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "claimed_items")
public class ClaimedItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lost_item_id")
    private LostItem lostItem;

    private Long userId;
    private int quantity;
    LocalDateTime claimedAt;

}
