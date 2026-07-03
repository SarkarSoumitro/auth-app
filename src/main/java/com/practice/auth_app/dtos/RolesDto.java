package com.practice.auth_app.dtos;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RolesDto {
   // private UUID id = UUID.randomUUID();
    private UUID id;
    private String name ;
}
