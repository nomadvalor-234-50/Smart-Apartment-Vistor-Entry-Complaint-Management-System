package com.project.savecms;

import com.project.savecms.entity.Flat;
import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.repository.FlatRepository;
import com.project.savecms.repository.UserRepository;
import com.project.savecms.service.FlatService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FlatServiceTest {

    @Test
    void mapsBlockNameToLegacyRequiredBuildingColumn() {
        FlatRepository flatRepository = mock(FlatRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        User admin = new User();
        admin.setRole(UserRole.ADMIN);
        when(userRepository.findBySessionToken("admin-token"))
                .thenReturn(Optional.of(admin));
        when(flatRepository.existsByFlatNumber("A-101")).thenReturn(false);
        when(flatRepository.save(any(Flat.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Flat flat = new Flat();
        flat.setFlatNumber(" A-101 ");
        flat.setBlockName(" A ");
        flat.setFloorNumber(0);
        flat.setNumberOfRooms(3);

        Flat saved = new FlatService(flatRepository, userRepository)
                .createFlat("admin-token", flat);

        assertEquals("A", saved.getBuilding());
        assertEquals("A", saved.getBlockName());
        assertEquals("A-101", saved.getFlatNumber());
        ArgumentCaptor<Flat> savedFlat = ArgumentCaptor.forClass(Flat.class);
        verify(flatRepository).save(savedFlat.capture());
        assertEquals("A", savedFlat.getValue().getBuilding());
    }
}
