package com.project.savecms.service;

import com.project.savecms.entity.Flat;
import com.project.savecms.entity.Resident;
import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.repository.FlatRepository;
import com.project.savecms.repository.ResidentRepository;
import com.project.savecms.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final FlatRepository flatRepository;
    private final UserRepository userRepository;

    public ResidentService(ResidentRepository residentRepository,
                           FlatRepository flatRepository,
                           UserRepository userRepository) {
        this.residentRepository = residentRepository;
        this.flatRepository = flatRepository;
        this.userRepository = userRepository;
    }

    public Resident createResident(Resident resident, Long flatId, String token) {
        validateAdmin(token);
        validateResident(resident);

        if (residentRepository.existsByEmail(resident.getEmail())) {
            throw new IllegalArgumentException("Resident email already exists.");
        }

        if (residentRepository.existsByPhone(resident.getPhone())) {
            throw new IllegalArgumentException("Resident phone number already exists.");
        }

        Flat flat = flatRepository.findById(flatId)
                .orElseThrow(() -> new IllegalArgumentException("Flat not found."));

        resident.setFlat(flat);
        return residentRepository.save(resident);
    }

    public List<Resident> getAllResidents(String token) {
        validateAdmin(token);
        return residentRepository.findAll();
    }

    public Resident updateResident(Long residentId, Resident updatedResident,
                                   Long flatId, String token) {
        validateAdmin(token);
        validateResident(updatedResident);

        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found."));

        Flat flat = flatRepository.findById(flatId)
                .orElseThrow(() -> new IllegalArgumentException("Flat not found."));

        resident.setFullName(updatedResident.getFullName());
        resident.setEmail(updatedResident.getEmail());
        resident.setPhone(updatedResident.getPhone());
        resident.setEmergencyContact(updatedResident.getEmergencyContact());
        resident.setNumberOfOccupants(updatedResident.getNumberOfOccupants());
        resident.setFlat(flat);

        return residentRepository.save(resident);
    }

    private void validateAdmin(String token) {
        User user = userRepository.findBySessionToken(token)
                .orElseThrow(() -> new SecurityException("Invalid or expired session token."));

        if (user.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Admin access required.");
        }
    }

    private void validateResident(Resident resident) {
        if (resident.getFullName() == null || resident.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        if (resident.getEmail() == null || resident.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (resident.getPhone() == null || resident.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        if (resident.getEmergencyContact() == null || resident.getEmergencyContact().trim().isEmpty()) {
            throw new IllegalArgumentException("Emergency contact is required.");
        }

        if (resident.getNumberOfOccupants() == null || resident.getNumberOfOccupants() < 1) {
            throw new IllegalArgumentException("Number of occupants must be at least 1.");
        }
    }
}