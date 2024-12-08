package com.assignment.lostandfound.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class UserIdsRequest {
    private List<Long> ids;
}
