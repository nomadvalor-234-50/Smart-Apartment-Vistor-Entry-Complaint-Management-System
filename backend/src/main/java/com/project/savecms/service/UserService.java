package com.project.savecms.service;

import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User registerUser(User user) {

        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException(
                    "Administrator accounts can only be created through the configured bootstrap.");
        }

        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }

        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters.");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("This email is already registered.");
        }

        if (userRepository.existsByPhone(user.getPhone())) {
            throw new IllegalArgumentException("This phone number is already registered.");
        }

        if (user.getRole() == null) {
            user.setRole(UserRole.RESIDENT);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == UserRole.SECURITY_GUARD
                || user.getRole() == UserRole.MAINTENANCE_STAFF
                || user.getRole() == UserRole.APARTMENT_MANAGER) {
            user.setAccountStatus("PENDING");
        } else {
            user.setAccountStatus("ACTIVE");
        }

        return userRepository.save(user);
    }

    public User loginUser(String email, String password) {

        if (email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("Invalid email or password."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new SecurityException("Invalid email or password.");
        }

        if (!"ACTIVE".equals(user.getAccountStatus())) {
            throw new IllegalStateException("Account is pending administrator approval.");
        }

        user.setSessionToken(UUID.randomUUID().toString());

        return userRepository.save(user);
    }

    public void logoutUser(String sessionToken) {

        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            throw new SecurityException("Invalid or expired session token.");
        }

        User user = userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() ->
                        new SecurityException("Invalid or expired session token."));

        user.setSessionToken(null);

        userRepository.save(user);
    }

    public List<User> getAllUsers(String sessionToken) {
        validateAdmin(sessionToken);
        return userRepository.findAll();
    }

    public User approveUser(String sessionToken, Long userId) {
        validateAdmin(sessionToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        user.setAccountStatus("ACTIVE");

        return userRepository.save(user);
    }

    public User changeUserRole(String sessionToken, Long userId, UserRole newRole) {
        validateAdmin(sessionToken);

        if (newRole == null) {
            throw new IllegalArgumentException("A valid role is required.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        user.setRole(newRole);

        return userRepository.save(user);
    }

    private void validateAdmin(String sessionToken) {

        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            throw new SecurityException("Invalid or expired session token.");
        }

        User currentUser = userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() ->
                        new SecurityException("Invalid or expired session token."));

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException("Only administrators can perform this action.");
        }
    }
    public void deleteUser(Long userId, String token) {
        User admin = userRepository.findBySessionToken(token)
                .orElseThrow(() ->
                        new SecurityException("Invalid or expired session token."));
    
        if (admin.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Admin access required.");
        }
    
        if (admin.getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Admin cannot delete their own account.");
        }
    
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    
        userRepository.delete(user);
    }
}