package com.practice.auth_app.repositories;

import com.practice.auth_app.entities.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RolesRepository extends JpaRepository<Roles, UUID> {
    Optional<Roles> findByName(String name);
    List<Roles> findByNameIn(Set<String> names);
}
