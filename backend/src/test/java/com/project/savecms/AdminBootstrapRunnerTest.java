package com.project.savecms;

import com.project.savecms.config.AdminBootstrapRunner;
import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.repository.UserRepository;
import com.project.savecms.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminBootstrapRunnerTest {

    private final ApplicationArguments arguments = mock(ApplicationArguments.class);

    @Test
    void createsActiveAdminWithHashedPasswordWhenNoAdminExists() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(repository.existsByRole(UserRole.ADMIN)).thenReturn(false);
        when(repository.existsByPhone("555-0100")).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminBootstrapRunner runner = new AdminBootstrapRunner(
                repository, "Admin", "admin@example.com", "555-0100",
                "a-strong-password");
        runner.run(arguments);

        var savedUser = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(repository).save(savedUser.capture());
        User admin = savedUser.getValue();
        assertEquals(UserRole.ADMIN, admin.getRole());
        assertEquals("ACTIVE", admin.getAccountStatus());
        assertTrue(new BCryptPasswordEncoder().matches(
                "a-strong-password", admin.getPassword()));
    }

    @Test
    void rejectsPublicAdminRegistration() {
        UserRepository repository = mock(UserRepository.class);
        UserService service = new UserService(repository);
        User user = new User();
        user.setRole(UserRole.ADMIN);

        assertThrows(IllegalArgumentException.class, () -> service.registerUser(user));
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void failsForPartialBootstrapConfiguration() {
        AdminBootstrapRunner runner = new AdminBootstrapRunner(
                mock(UserRepository.class), "Admin", "", "", "");

        assertThrows(IllegalStateException.class, () -> runner.run(arguments));
    }

    @Test
    void doesNotCreateAnotherAdminIfOneAlreadyExists() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(repository.existsByRole(UserRole.ADMIN)).thenReturn(true);

        AdminBootstrapRunner runner = new AdminBootstrapRunner(
                repository, "Admin", "admin@example.com", "555-0100",
                "a-strong-password");
        runner.run(arguments);

        verify(repository, never()).existsByPhone("555-0100");
        verify(repository, never()).save(any(User.class));
    }
}
