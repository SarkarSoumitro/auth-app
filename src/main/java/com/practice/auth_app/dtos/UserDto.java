package com.practice.auth_app.dtos;

import com.practice.auth_app.entities.helpers.Provider;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private UUID id;
    private String name;
    private String email;
    private String password;
    private String image;
    private boolean enable;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private Provider provider=Provider.LOCAL;
    private Set<RolesDto> role = new HashSet<>();
}
