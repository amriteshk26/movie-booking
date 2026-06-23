package com.amritesh.moviebooking.mapper;

import com.amritesh.moviebooking.dto.response.UserResponse;
import com.amritesh.moviebooking.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles()
        );
    }
}
