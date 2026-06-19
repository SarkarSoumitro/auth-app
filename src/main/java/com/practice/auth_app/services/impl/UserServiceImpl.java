package com.practice.auth_app.services.impl;

import com.practice.auth_app.dtos.UserDto;
import com.practice.auth_app.entities.User;
import com.practice.auth_app.entities.helpers.Provider;
import com.practice.auth_app.repositories.UserRepository;
import com.practice.auth_app.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        if(userDto.getEmail()==null ||userDto.getEmail().isBlank()){
            throw new IllegalStateException("Email is required");
        }
        if(userRepository.existByEmail(userDto.getEmail())){
            throw new IllegalStateException("Email already exist!");
        }
        User user = modelMapper.map(userDto, User.class);
        user.setProvider(userDto.getProvider()!=null? userDto.getProvider(): Provider.LOCAL);

        User savedUser= userRepository.save(user);
        return modelMapper.map(savedUser,UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found with this email "+ email));
        return modelMapper.map(user,UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String email) {
           User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found with email: " + email));
           user.setName(userDto.getName());
           user.setEmail(userDto.getEmail());
           user.setPassword(userDto.getPassword());
           user.setImage(userDto.getImage());
           user.setEnable(userDto.isEnable());
           user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);

           User updatedUser = userRepository.save(user);
           return modelMapper.map(updatedUser,UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {

    }

    @Override
    public UserDto getUserById(String userId) {
        return null;
    }

    @Override
    public Iterable<UserDto> getAllUser() {
        return null;
    }
}
