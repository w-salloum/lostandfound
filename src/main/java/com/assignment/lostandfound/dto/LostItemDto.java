package com.assignment.lostandfound.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LostItemDto {
    private String name;
    private String place;
    private int quantity;
}
