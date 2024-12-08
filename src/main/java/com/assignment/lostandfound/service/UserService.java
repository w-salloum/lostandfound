package com.assignment.lostandfound.service;

import com.assignment.lostandfound.client.UserClient;
import com.assignment.lostandfound.dto.UserDetailsDto;
import com.assignment.lostandfound.dto.UserIdsRequest;
import com.assignment.lostandfound.exception.UserServiceException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserClient userClient;

    public UserService(UserClient userClient) {
        this.userClient = userClient;
    }

    public boolean isUserExist(Long userId) {
        try {
            // Try to fetch user details
            if (userClient.getUserById(userId) != null) {
                return true;
            }
        } catch (feign.FeignException.NotFound e) {
            // Handle user not found (404)
                return false; // User not found
            }
        catch (Exception e) {
            throw new UserServiceException("Invalid user ids provided, or service is down");
        }
        return false;
    }

    public List<UserDetailsDto> getUsersByIds(List<Long> userIds) {
        try{
            return userClient.getUsersByIds(UserIdsRequest.builder().ids(userIds).build());
        } catch (Exception e) {
            throw new UserServiceException("Invalid user ids provided, or service is down");
        }
    }
}

