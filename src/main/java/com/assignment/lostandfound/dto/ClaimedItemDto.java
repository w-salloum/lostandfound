package com.assignment.lostandfound.dto;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ClaimedItemDto {
    Long lostItemId;
    String claimedBy;
    int quantity;
    String claimedAt;
}
