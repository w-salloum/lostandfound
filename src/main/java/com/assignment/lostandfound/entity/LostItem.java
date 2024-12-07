package com.assignment.lostandfound.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "lost_items")
public class LostItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "place_id")
    private Place place;

    private int quantity;
}
