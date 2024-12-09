package com.assignment.lostandfound.client;


import com.assignment.lostandfound.dto.UserDetailsDto;
import com.assignment.lostandfound.dto.UserIdsRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service", url = "localhost:8080")
public interface UserClient {
    // return user details by user id
    @GetMapping("/users/{userId}")
    UserDetailsDto getUserById(@PathVariable Long userId);

    // return user details by list of user ids
    @PostMapping("/users/query")
    List<UserDetailsDto> getUsersByIds(@RequestBody UserIdsRequest userIdsRequest);
}
