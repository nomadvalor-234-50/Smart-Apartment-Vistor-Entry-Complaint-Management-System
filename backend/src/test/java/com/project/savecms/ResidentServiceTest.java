package com.project.savecms;

import com.project.savecms.entity.Flat;
import com.project.savecms.entity.Resident;
import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.repository.FlatRepository;
import com.project.savecms.repository.ResidentRepository;
import com.project.savecms.repository.UserRepository;
import com.project.savecms.service.ResidentService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResidentServiceTest {

    @Test
    void copiesPhoneToLegacyRequiredContactNumberColumn() {
        ResidentRepository residentRepository = mock(ResidentRepository.class);
        FlatRepository flatRepository = mock(FlatRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        User admin = new User();
        admin.setRole(UserRole.ADMIN);
        when(userRepository.findBySessionToken("admin-token"))
                .thenReturn(Optional.of(admin));
        when(residentRepository.existsByEmail("james@example.com")).thenReturn(false);
        when(residentRepository.existsByPhone("1234567890")).thenReturn(false);
        when(flatRepository.findById(1L)).thenReturn(Optional.of(new Flat()));
        when(residentRepository.save(any(Resident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Resident resident = new Resident();
        resident.setFullName(" James ");
        resident.setEmail("JAMES@example.com");
        resident.setPhone("1234567890");
        resident.setEmergencyContact("0987654321");
        resident.setNumberOfOccupants(3);

        new ResidentService(residentRepository, flatRepository, userRepository)
                .createResident(resident, 1L, "admin-token");

        ArgumentCaptor<Resident> savedResident = ArgumentCaptor.forClass(Resident.class);
        verify(residentRepository).save(savedResident.capture());
        assertEquals("1234567890", savedResident.getValue().getPhone());
        assertEquals("1234567890", savedResident.getValue().getContactNumber());
    }
}
