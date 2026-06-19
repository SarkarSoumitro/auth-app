package com.practice.auth_app.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "Roles")
public class Roles {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(name = "Role",nullable = false)
    private String name ;
}
