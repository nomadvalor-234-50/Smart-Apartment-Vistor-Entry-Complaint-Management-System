package com.project.savecms.service;

import com.project.savecms.entity.Flat;
import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.repository.FlatRepository;
import com.project.savecms.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class QRService {

    private final FlatRepository flatRepository;
    private final UserRepository userRepository;

    public QRService(FlatRepository flatRepository, UserRepository userRepository) {
        this.flatRepository = flatRepository;
        this.userRepository = userRepository;
    }

    public Flat generateFlatQrCode(Long flatId, String token) {
        validateAdmin(token);

        Flat flat = flatRepository.findById(flatId)
                .orElseThrow(() -> new IllegalArgumentException("Flat not found."));

        if (flat.getQrCode() == null || flat.getQrCode().isBlank()) {
            flat.setQrCode("FLAT-" + UUID.randomUUID());
            flatRepository.save(flat);
        }

        return flat;
    }

    public Flat getFlatQrCode(Long flatId, String token) {
        validateAdmin(token);

        Flat flat = flatRepository.findById(flatId)
                .orElseThrow(() -> new IllegalArgumentException("Flat not found."));

        if (flat.getQrCode() == null || flat.getQrCode().isBlank()) {
            throw new IllegalArgumentException("QR code has not been generated for this flat.");
        }

        return flat;
    }

    public Flat validateFlatQrCode(String qrCode, String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new SecurityException("Invalid or expired session token.");
        }

        User user = userRepository.findBySessionToken(token.trim())
                .orElseThrow(() -> new SecurityException(
                        "Invalid or expired session token."));

        if (user.getRole() != UserRole.ADMIN
                && user.getRole() != UserRole.SECURITY_GUARD) {
            throw new SecurityException(
                    "Security guard or admin access required.");
        }

        if (qrCode == null || qrCode.trim().isEmpty()) {
            throw new IllegalArgumentException("A flat QR identifier is required.");
        }

        return flatRepository.findByQrCode(qrCode.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "QR identifier is invalid or does not belong to a registered flat."));
    }

    private void validateAdmin(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new SecurityException("Invalid or expired session token.");
        }

        User user = userRepository.findBySessionToken(token.trim())
                .orElseThrow(() -> new SecurityException("Invalid or expired session token."));

        if (user.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Admin access required.");
        }
    }
}