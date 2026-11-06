package com.project.savecms.config;

import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger logger =
            LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();
    private final String fullName;
    private final String email;
    private final String phone;
    private final String password;

    public AdminBootstrapRunner(
            UserRepository userRepository,
            @Value("${app.bootstrap-admin.full-name:}") String fullName,
            @Value("${app.bootstrap-admin.email:}") String email,
            @Value("${app.bootstrap-admin.phone:}") String phone,
            @Value("${app.bootstrap-admin.password:}") String password) {
        this.userRepository = userRepository;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (isUnconfigured()) {
            logger.info("Admin bootstrap is not configured; no admin account was created.");
            return;
        }

        validateConfiguration();

        Optional<User> configuredAdmin = userRepository.findByEmail(email);
        if (configuredAdmin.isPresent()) {
            User existingUser = configuredAdmin.get();
            if (existingUser.getRole() != UserRole.ADMIN) {
                throw new IllegalStateException(
                        "The configured bootstrap email already belongs to a non-admin user.");
            }
            logger.info("The configured bootstrap admin already exists; no changes were made.");
            return;
        }

        if (userRepository.existsByRole(UserRole.ADMIN)) {
            logger.info("An admin account already exists; bootstrap did not create another.");
            return;
        }

        if (userRepository.existsByPhone(phone)) {
            throw new IllegalStateException(
                    "The configured bootstrap phone number is already registered.");
        }

        User admin = new User();
        admin.setFullName(fullName.trim());
        admin.setEmail(email.trim());
        admin.setPhone(phone.trim());
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(UserRole.ADMIN);
        admin.setAccountStatus("ACTIVE");
        userRepository.save(admin);
        logger.info("The initial admin account was created.");
    }

    private boolean isUnconfigured() {
        return isBlank(fullName) && isBlank(email) && isBlank(phone) && isBlank(password);
    }

    private void validateConfiguration() {
        if (isBlank(fullName) || isBlank(email)
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || isBlank(phone) || password == null || password.length() < 12) {
            throw new IllegalStateException(
                    "Admin bootstrap requires a full name, valid email, phone, and a password of at least 12 characters.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
