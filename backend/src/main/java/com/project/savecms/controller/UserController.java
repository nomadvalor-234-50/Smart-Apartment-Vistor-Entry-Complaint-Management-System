package com.project.savecms.controller;

import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            User savedUser = userService.registerUser(user);

            response.put("message", "User registered successfully.");
            response.putAll(toUserResponse(savedUser));

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IllegalArgumentException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(
            @RequestBody Map<String, String> loginRequest) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            User loggedInUser = userService.loginUser(
                    loginRequest.get("email"),
                    loginRequest.get("password")
            );

            response.put("message", "User logged in successfully.");
            response.putAll(toUserResponse(loggedInUser));
            response.put("token", loggedInUser.getSessionToken());

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IllegalStateException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);

        } catch (SecurityException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

        } catch (IllegalArgumentException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logoutUser(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            userService.logoutUser(extractToken(authorizationHeader));

            response.put("message", "User logged out successfully.");

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (SecurityException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            List<Map<String, Object>> users = userService
                    .getAllUsers(extractToken(authorizationHeader))
                    .stream()
                    .map(this::toUserResponse)
                    .toList();

            response.put("message", "Users retrieved successfully.");
            response.put("users", users);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IllegalStateException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);

        } catch (SecurityException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveUser(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            User approvedUser = userService.approveUser(
                    extractToken(authorizationHeader),
                    id
            );

            response.put("message", "User approved successfully.");
            response.putAll(toUserResponse(approvedUser));

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IllegalStateException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);

        } catch (SecurityException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

        } catch (IllegalArgumentException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Map<String, Object>> changeUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> roleRequest,
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            String requestedRole = roleRequest.get("role");

            UserRole newRole = UserRole.valueOf(requestedRole.toUpperCase());

            User updatedUser = userService.changeUserRole(
                    extractToken(authorizationHeader),
                    id,
                    newRole
            );

            response.put("message", "User role updated successfully.");
            response.putAll(toUserResponse(updatedUser));

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IllegalStateException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);

        } catch (SecurityException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

        } catch (IllegalArgumentException exception) {
            response.put("message", "Invalid role or user ID.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            throw new SecurityException("Invalid or expired session token.");
        }

        return authorizationHeader.substring(7);
    }

    private Map<String, Object> toUserResponse(User user) {
        Map<String, Object> userResponse = new LinkedHashMap<>();

        userResponse.put("userId", user.getId());
        userResponse.put("fullName", user.getFullName());
        userResponse.put("email", user.getEmail());
        userResponse.put("phone", user.getPhone());
        userResponse.put("role", user.getRole());
        userResponse.put("address", user.getAddress());
        userResponse.put("accountStatus", user.getAccountStatus());

        return userResponse;
    }
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long userId,
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {
            
        try {
            userService.deleteUser(userId, extractToken(authorization));
        
            return ResponseEntity.ok(Map.of(
                    "message", "User deleted successfully."
            ));
        
        } catch (SecurityException e) {
            return securityResponse(e);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
    private ResponseEntity<?> securityResponse(SecurityException e) {
        if (e.getMessage().equals("Admin access required.")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", e.getMessage()));
    }
}