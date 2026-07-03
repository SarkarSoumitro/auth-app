package com.practice.auth_app.services;

import com.practice.auth_app.dtos.RolesDto;
import com.practice.auth_app.dtos.UserDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AdminService {
    List<UserDto> getAllUsers();
    UserDto getUserById(UUID id);
    UserDto getUserByEmail(String email);
    UserDto replaceUserRoles(String email, Set<String> roleNames);
    UserDto addRolesToUser(String email, Set<String> roleNames);
    UserDto removeRoleFromUser(String email, String roleName);
    void revokeRefreshTokensForUser(String email);
    List<RolesDto> getAllRoles();
    RolesDto createRole(RolesDto roleDto);
}
