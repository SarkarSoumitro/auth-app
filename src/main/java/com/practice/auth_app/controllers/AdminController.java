package com.practice.auth_app.controllers;

import com.practice.auth_app.dtos.RoleNamesRequest;
import com.practice.auth_app.dtos.RolesDto;
import com.practice.auth_app.dtos.UserDto;
import com.practice.auth_app.services.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin")
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserDto>> listUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{email}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(adminService.getUserByEmail(email));
    }

    @PatchMapping("/users/{email}/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> replaceRoles(
            @PathVariable String email,
            @RequestBody RoleNamesRequest req
    ) {
        return ResponseEntity.ok(adminService.replaceUserRoles(email, req.getRoles()));
    }

    @PostMapping("/users/{email}/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> addRoles(
            @PathVariable String email,
            @RequestBody RoleNamesRequest req
    ) {
        return ResponseEntity.ok(adminService.addRolesToUser(email, req.getRoles()));
    }

    @DeleteMapping("/users/{email}/roles/{roleName}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> removeRole(
            @PathVariable String email,
            @PathVariable String roleName
    ) {
        return ResponseEntity.ok(adminService.removeRoleFromUser(email, roleName));
    }

    @PostMapping("/users/{email}/revoke-tokens")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> revokeTokens(@PathVariable String email) {
        adminService.revokeRefreshTokensForUser(email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<RolesDto>> listRoles() {
        return ResponseEntity.ok(adminService.getAllRoles());
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RolesDto> createRole(@RequestBody RolesDto roleDto) {
        return ResponseEntity.ok(adminService.createRole(roleDto));
    }

    @DeleteMapping("/roles/{roleName}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteRole(@PathVariable String roleName) {
        adminService.deleteRole(roleName);
        return ResponseEntity.noContent().build();
    }
}
