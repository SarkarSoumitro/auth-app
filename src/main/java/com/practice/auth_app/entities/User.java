package com.practice.auth_app.entities;


import com.practice.auth_app.entities.helpers.Provider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(length = 20)
    private String name;
    @Column(unique = true)
    private String email;
    @Column(length = 50)
    private String password;
    @Column
    private String image;
    @Column
    private boolean enable;
    @Column
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Provider provider=Provider.LOCAL;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns=@JoinColumn(name = "user_id") ,
            inverseJoinColumns = @JoinColumn(name = "roles_id"))
    private Set<Roles> role = new HashSet<>();
    @PrePersist
    protected  void onCreate(){
        LocalDateTime now = LocalDateTime.now();
        if(createdAt== null) createdAt=now;
        updatedAt=now;
    }

    @PreUpdate
    protected void onUpdate(){
        LocalDateTime now = LocalDateTime.now();
        updatedAt=now;
    }

}
