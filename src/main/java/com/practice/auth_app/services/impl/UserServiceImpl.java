package com.practice.auth_app.services.impl;

import com.practice.auth_app.dtos.UserDto;
import com.practice.auth_app.entities.User;
import com.practice.auth_app.entities.helpers.Provider;
import com.practice.auth_app.exceptions.ResourceAlreadyExistException;
import com.practice.auth_app.exceptions.ResourceNotFoundException;
import com.practice.auth_app.repositories.UserRepository;
import com.practice.auth_app.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        if(userDto.getEmail()==null ||userDto.getEmail().isBlank()){
            throw new IllegalArgumentException("Email is required");
        }
        if(userRepository.existsByEmail(userDto.getEmail())){
            throw new ResourceAlreadyExistException("Email already exist!");
        }
        User user = modelMapper.map(userDto, User.class);
        user.setProvider(userDto.getProvider()!=null? userDto.getProvider(): Provider.LOCAL);

        User savedUser= userRepository.save(user);
        return modelMapper.map(savedUser,UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User not found with this email "+ email));
        return modelMapper.map(user,UserDto.class);
    }

    @Override
    @Transactional
    public UserDto updateUser(UserDto userDto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email
                ));

        String newEmail = userDto.getEmail();

        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new ResourceAlreadyExistException(
                    "User already exists with this email " + newEmail
            );
        }

        user.setName(userDto.getName());
        user.setEmail(newEmail);
        //TODO: update pass logic ... to hashcode
        user.setPassword(userDto.getPassword());
        user.setImage(userDto.getImage());
        user.setEnable(userDto.isEnable());
        user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);

        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {
        UUID id = UUID.fromString(userId);
        User user = userRepository.findById(id).orElseThrow(()->  new ResourceNotFoundException("user not found associate with this is "+ userId));
        userRepository.delete(user);
    }

    @Override
    public UserDto getUserById(String userId) {
        UUID id = UUID.fromString(userId);
        User user = userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User not found associate with this id "+ userId));
        return modelMapper.map(user,UserDto.class);
    }

    @Override
    public Iterable<UserDto> getAllUser() {
       return userRepository.
               findAll().
               stream().
               map(user ->modelMapper.map(user,UserDto.class)).
               toList();
    }
}
