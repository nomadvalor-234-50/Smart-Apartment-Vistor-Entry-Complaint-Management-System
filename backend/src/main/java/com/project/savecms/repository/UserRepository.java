package com.project.savecms.repository;

import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByRole(UserRole role);

    Optional<User> findByEmail(String email);

    Optional<User> findBySessionToken(String sessionToken);
}