package com.assignment.lostandfound.client;


import com.assignment.lostandfound.dto.UserDetailsDto;
import com.assignment.lostandfound.dto.UserIdsRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserClient {
    @GetMapping("/users/{userId}")
    UserDetailsDto getUserById(@PathVariable Long userId);

    @PostMapping("/users/query")
    List<UserDetailsDto> getUsersByIds(@RequestBody UserIdsRequest userIdsRequest);
}
