package com.practice.auth_app.repositories;

import com.practice.auth_app.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByJti(String jti);
    List<RefreshToken> findByUser_Id(UUID userId);
}
