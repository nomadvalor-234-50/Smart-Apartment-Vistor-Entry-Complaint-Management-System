package com.project.savecms;

import com.project.savecms.entity.Flat;
import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.entity.Visitor;
import com.project.savecms.repository.FlatRepository;
import com.project.savecms.repository.UserRepository;
import com.project.savecms.repository.VisitorRepository;
import com.project.savecms.service.VisitorService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VisitorServiceTest {

    private final VisitorRepository visitorRepository = mock(VisitorRepository.class);
    private final FlatRepository flatRepository = mock(FlatRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final VisitorService visitorService =
            new VisitorService(visitorRepository, flatRepository, userRepository);

    @Test
    void guardCanRecordEntryOnlyForFlatWithRegisteredQr() {
        User guard = user(UserRole.SECURITY_GUARD);
        Flat flat = new Flat();
        flat.setFlatNumber("A-101");
        flat.setQrCode("flat-qr");
        when(userRepository.findBySessionToken("guard-token"))
                .thenReturn(Optional.of(guard));
        when(flatRepository.findByQrCode("flat-qr")).thenReturn(Optional.of(flat));
        when(visitorRepository.save(any(Visitor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Visitor visitor = visitor();

        Visitor saved = visitorService.recordEntry(visitor, " flat-qr ", "guard-token");

        assertEquals("IN", saved.getStatus());
        assertEquals("A-101", saved.getFlat().getFlatNumber());
        assertEquals("Visitor Name", saved.getFullName());
        assertEquals("1234567890", saved.getContactNumber());
        assertNotNull(saved.getEntryTime());
        verify(visitorRepository).save(visitor);
    }

    @Test
    void rejectsUnknownQrWithoutSavingEntry() {
        when(userRepository.findBySessionToken("guard-token"))
                .thenReturn(Optional.of(user(UserRole.SECURITY_GUARD)));
        when(flatRepository.findByQrCode("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> visitorService.recordEntry(visitor(), "unknown", "guard-token"));
        verify(visitorRepository, never()).save(any(Visitor.class));
    }

    @Test
    void onlySecurityGuardCanRecordEntryOrExit() {
        when(userRepository.findBySessionToken("admin-token"))
                .thenReturn(Optional.of(user(UserRole.ADMIN)));

        assertThrows(SecurityException.class,
                () -> visitorService.recordEntry(visitor(), "flat-qr", "admin-token"));
        assertThrows(SecurityException.class,
                () -> visitorService.recordExit(1L, "admin-token"));
    }

    @Test
    void recordsExitTimeForActiveVisitAndRejectsDuplicateExit() {
        User guard = user(UserRole.SECURITY_GUARD);
        Visitor activeVisit = visitor();
        activeVisit.setStatus("IN");
        when(userRepository.findBySessionToken("guard-token"))
                .thenReturn(Optional.of(guard));
        when(visitorRepository.findById(1L)).thenReturn(Optional.of(activeVisit));
        when(visitorRepository.save(any(Visitor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Visitor exited = visitorService.recordExit(1L, "guard-token");

        assertEquals("OUT", exited.getStatus());
        assertNotNull(exited.getExitTime());
        assertTrue(exited.getExitTime().isAfter(LocalDateTime.MIN));

        assertThrows(IllegalArgumentException.class,
                () -> visitorService.recordExit(1L, "guard-token"));
    }

    private User user(UserRole role) {
        User user = new User();
        user.setRole(role);
        return user;
    }

    private Visitor visitor() {
        Visitor visitor = new Visitor();
        visitor.setVisitorName("Visitor Name");
        visitor.setPhone("1234567890");
        visitor.setPurpose("Delivery");
        return visitor;
    }
}
