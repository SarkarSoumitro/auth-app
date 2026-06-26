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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto createUser(UserDto userDto) {
        String email = normalizeEmail(userDto.getEmail());

        if(email == null){
            throw new IllegalArgumentException("Email is required");
        }
        if(userRepository.existsByEmail(email)){
            throw new ResourceAlreadyExistException("Email already exist!");
        }
        User user = modelMapper.map(userDto, User.class);
        user.setEmail(email);
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
        String currentEmail = normalizeEmail(email);
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + currentEmail
                ));

        String newEmail = normalizeEmail(userDto.getEmail());

        if (newEmail == null) {
            throw new IllegalArgumentException("Email is required");
        }

        if (userRepository.existsByEmailAndIdNot(newEmail, user.getId())) {
            throw new ResourceAlreadyExistException(
                    "User already exists with this email " + newEmail
            );
        }

        user.setName(userDto.getName());
        user.setEmail(newEmail);
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        user.setImage(userDto.getImage());
        user.setEnable(userDto.isEnable());
        user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);

        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        String normalizedEmail = email.trim();
        return normalizedEmail.isBlank() ? null : normalizedEmail;
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
