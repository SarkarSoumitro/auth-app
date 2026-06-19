package com.practice.auth_app.services;

import com.practice.auth_app.dtos.UserDto;

public interface UserService {
    //create user
    UserDto createUser(UserDto userDto);
    //get user by email
     UserDto getUserByEmail(String email);
    //update user
    UserDto updateUser(UserDto  userDto , String email);
    //delete user
    void deleteUser(String userId);
    //get users by id
    UserDto getUserById(String userId);
    //get all users
    Iterable<UserDto>getAllUser();
}
