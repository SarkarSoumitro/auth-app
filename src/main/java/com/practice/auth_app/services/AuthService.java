package com.practice.auth_app.services;

import com.practice.auth_app.dtos.UserDto;

public interface AuthService {
    UserDto registerUser(UserDto userDto);
}
