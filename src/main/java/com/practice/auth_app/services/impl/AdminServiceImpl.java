package com.practice.auth_app.services.impl;

import com.practice.auth_app.dtos.RolesDto;
import com.practice.auth_app.dtos.UserDto;
import com.practice.auth_app.entities.RefreshToken;
import com.practice.auth_app.entities.Roles;
import com.practice.auth_app.entities.User;
import com.practice.auth_app.exceptions.ResourceNotFoundException;
import com.practice.auth_app.repositories.RefreshTokenRepository;
import com.practice.auth_app.repositories.RolesRepository;
import com.practice.auth_app.repositories.UserRepository;
import com.practice.auth_app.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ModelMapper mapper;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(u -> mapper.map(u, UserDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(UUID id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapper.map(u, UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapper.map(u, UserDto.class);
    }

    @Override
    @Transactional
    public UserDto replaceUserRoles(String email, Set<String> roleNames) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        List<Roles> roles = rolesRepository.findByNameIn(roleNames);
        if (roles.size() != roleNames.size()) {
            Set<String> found = roles.stream().map(Roles::getName).collect(Collectors.toSet());
            Set<String> missing = new HashSet<>(roleNames);
            missing.removeAll(found);
            throw new ResourceNotFoundException("Roles not found: " + missing);
        }
        user.setRoles(new HashSet<>(roles));
        User saved = userRepository.save(user);
        return mapper.map(saved, UserDto.class);
    }

    @Override
    @Transactional
    public UserDto addRolesToUser(String email, Set<String> roleNames) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        List<Roles> roles = rolesRepository.findByNameIn(roleNames);
        Set<Roles> current = user.getRoles() == null ? new HashSet<>() : user.getRoles();
        current.addAll(roles);
        user.setRoles(current);
        return mapper.map(userRepository.save(user), UserDto.class);
    }

    @Override
    @Transactional
    public UserDto removeRoleFromUser(String email, String roleName) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        Roles role = rolesRepository.findByName(roleName).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        Set<Roles> current = user.getRoles();
        if (current != null) {
            current.removeIf(r -> r.getName().equals(role.getName()));
            user.setRoles(current);
            return mapper.map(userRepository.save(user), UserDto.class);
        }
        return mapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public void revokeRefreshTokensForUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        List<RefreshToken> tokens = refreshTokenRepository.findByUser_Id(user.getId());
        tokens.forEach(t -> {
            t.setRevoked(true);
            t.setReplacedByToken(null);
        });
        refreshTokenRepository.saveAll(tokens);
    }

    @Override
    public List<RolesDto> getAllRoles() {
        return rolesRepository.findAll()
                .stream()
                .map(r -> new RolesDto(r.getId(), r.getName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RolesDto createRole(RolesDto roleDto) {
        rolesRepository.findByName(roleDto.getName()).ifPresent(r -> {
            throw new com.practice.auth_app.exceptions.ResourceAlreadyExistException("Role already exists: " + roleDto.getName());
        });

        Roles role = Roles.builder()
               // .id(UUID.randomUUID())
                .name(roleDto.getName())
                .build();
        
        Roles saved = rolesRepository.save(role);
        return new RolesDto(saved.getId(), saved.getName());
    }
}
