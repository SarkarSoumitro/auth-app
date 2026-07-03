package com.practice.auth_app.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private boolean enable = true;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    private Provider provider=Provider.LOCAL;
    private Set<RolesDto> roles = new HashSet<>();
}
