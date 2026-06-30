package com.practice.auth_app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens",indexes = {
         @Index(name = "refresh_token_jti_idx",columnList = "jti",unique = true),
        @Index(name = "refresh_token_user_idx",columnList = "user_id")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "jti",unique = true, nullable = false,updatable = false)
    private String jti;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(updatable = false,nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiredAt;

    @Column(nullable = false)
    private boolean revoked;

    private String replacedByToken;

}
