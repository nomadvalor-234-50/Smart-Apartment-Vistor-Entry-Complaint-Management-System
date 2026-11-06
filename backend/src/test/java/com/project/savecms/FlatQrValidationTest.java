package com.project.savecms;

import com.project.savecms.entity.Flat;
import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.repository.FlatRepository;
import com.project.savecms.repository.UserRepository;
import com.project.savecms.service.QRService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FlatQrValidationTest {

    private final FlatRepository flatRepository = mock(FlatRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final QRService qrService = new QRService(flatRepository, userRepository);

    @Test
    void securityGuardCanValidateRegisteredFlatQr() {
        User guard = new User();
        guard.setRole(UserRole.SECURITY_GUARD);
        Flat flat = new Flat();
        flat.setFlatNumber("A-101");
        flat.setQrCode("flat-qr-token");
        when(userRepository.findBySessionToken("guard-token")).thenReturn(Optional.of(guard));
        when(flatRepository.findByQrCode("flat-qr-token")).thenReturn(Optional.of(flat));

        Flat validated = qrService.validateFlatQrCode(" flat-qr-token ", "guard-token");

        assertEquals("A-101", validated.getFlatNumber());
        verify(flatRepository).findByQrCode("flat-qr-token");
    }

    @Test
    void residentCannotValidateFlatQr() {
        User resident = new User();
        resident.setRole(UserRole.RESIDENT);
        when(userRepository.findBySessionToken("resident-token"))
                .thenReturn(Optional.of(resident));

        assertThrows(SecurityException.class,
                () -> qrService.validateFlatQrCode("flat-qr-token", "resident-token"));
    }

    @Test
    void unknownQrIdentifierIsRejected() {
        User admin = new User();
        admin.setRole(UserRole.ADMIN);
        when(userRepository.findBySessionToken("admin-token"))
                .thenReturn(Optional.of(admin));
        when(flatRepository.findByQrCode("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> qrService.validateFlatQrCode("unknown", "admin-token"));
    }
}
