package com.example.projectservice.repository;

import com.example.projectservice.entity.AuthUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

  Optional<AuthUser> findByKeyHash(String keyHash);
}
