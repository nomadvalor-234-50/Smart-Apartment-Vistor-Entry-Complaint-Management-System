package com.project.savecms.service;

import com.project.savecms.entity.Flat;
import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.repository.FlatRepository;
import com.project.savecms.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlatService {

    private final FlatRepository flatRepository;
    private final UserRepository userRepository;

    public FlatService(FlatRepository flatRepository, UserRepository userRepository) {
        this.flatRepository = flatRepository;
        this.userRepository = userRepository;
    }

    public Flat createFlat(String token, Flat flat) {
        validateAdmin(token);
        validateFlatDetails(flat);

        if (flatRepository.existsByFlatNumber(flat.getFlatNumber())) {
            throw new IllegalArgumentException("Flat number already exists.");
        }

        return flatRepository.save(flat);
    }

    public List<Flat> getAllFlats(String token) {
        validateAdmin(token);
        return flatRepository.findAll();
    }

    public Flat updateFlat(String token, Long id, Flat flatDetails) {
        validateAdmin(token);
        validateFlatDetails(flatDetails);

        Flat existingFlat = flatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Flat not found."));

        if (!existingFlat.getFlatNumber().equals(flatDetails.getFlatNumber())
                && flatRepository.existsByFlatNumber(flatDetails.getFlatNumber())) {
            throw new IllegalArgumentException("Flat number already exists.");
        }

        existingFlat.setFlatNumber(flatDetails.getFlatNumber());
        existingFlat.setBlockName(flatDetails.getBlockName());
        existingFlat.setFloorNumber(flatDetails.getFloorNumber());
        existingFlat.setNumberOfRooms(flatDetails.getNumberOfRooms());

        return flatRepository.save(existingFlat);
    }

    private void validateFlatDetails(Flat flat) {
        if (flat.getFlatNumber() == null || flat.getFlatNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Flat number is required.");
        }

        if (flat.getBlockName() == null || flat.getBlockName().trim().isEmpty()) {
            throw new IllegalArgumentException("Block name is required.");
        }

        if (flat.getFloorNumber() == null || flat.getFloorNumber() < 0) {
            throw new IllegalArgumentException("A valid floor number is required.");
        }

        if (flat.getNumberOfRooms() == null || flat.getNumberOfRooms() < 1) {
            throw new IllegalArgumentException("Number of rooms must be at least 1.");
        }
    }

    private void validateAdmin(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new SecurityException("Invalid or expired session token.");
        }

        User user = userRepository.findBySessionToken(token)
                .orElseThrow(() ->
                        new SecurityException("Invalid or expired session token."));

        if (user.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException("Only administrators can perform this action.");
        }
    }
}