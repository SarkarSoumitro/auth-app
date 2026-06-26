package com.practice.auth_app.services.impl;

import com.practice.auth_app.dtos.UserDto;
import com.practice.auth_app.services.AuthService;
import com.practice.auth_app.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private  final PasswordEncoder passwordEncoder;

    @Override
    public UserDto registerUser(UserDto userDto) {

        //logic
        //verify email
        //verify password
        //default role
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        UserDto user = userService.createUser(userDto);
        return user;

    }
}
