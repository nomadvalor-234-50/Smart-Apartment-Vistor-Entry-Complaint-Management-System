package com.project.savecms.service;

import com.project.savecms.entity.Flat;
import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.entity.Visitor;
import com.project.savecms.repository.FlatRepository;
import com.project.savecms.repository.UserRepository;
import com.project.savecms.repository.VisitorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VisitorService {

    private final VisitorRepository visitorRepository;
    private final FlatRepository flatRepository;
    private final UserRepository userRepository;

    public VisitorService(VisitorRepository visitorRepository,
                          FlatRepository flatRepository,
                          UserRepository userRepository) {
        this.visitorRepository = visitorRepository;
        this.flatRepository = flatRepository;
        this.userRepository = userRepository;
    }

    public Visitor recordEntry(Visitor visitor, String qrCode, String token) {
        validateSecurityGuard(token);
        validateVisitor(visitor);

        if (qrCode == null || qrCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Scan a valid flat QR code before recording entry.");
        }

        Flat flat = flatRepository.findByQrCode(qrCode.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "QR identifier is invalid or does not belong to a registered flat."));

        visitor.setFlat(flat);
        visitor.setVisitorName(visitor.getVisitorName().trim());
        visitor.setFullName(visitor.getVisitorName());
        visitor.setPhone(visitor.getPhone().trim());
        visitor.setContactNumber(visitor.getPhone());
        visitor.setPurpose(visitor.getPurpose().trim());
        visitor.setEntryTime(LocalDateTime.now());
        visitor.setStatus("IN");

        return visitorRepository.save(visitor);
    }

    public List<Visitor> getAllVisitors(String token) {
        validateSecurityOrAdmin(token);
        return visitorRepository.findAllByOrderByEntryTimeDesc();
    }

    public Visitor recordExit(Long visitorId, String token) {
        validateSecurityGuard(token);

        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new IllegalArgumentException("Visitor entry not found."));

        if (!"IN".equals(visitor.getStatus()) || visitor.getExitTime() != null) {
            throw new IllegalArgumentException("Visitor has already exited.");
        }

        visitor.setExitTime(LocalDateTime.now());
        visitor.setStatus("OUT");

        return visitorRepository.save(visitor);
    }

    private void validateSecurityOrAdmin(String token) {
        User user = authenticatedUser(token);

        if (user.getRole() != UserRole.ADMIN
                && user.getRole() != UserRole.SECURITY_GUARD) {
            throw new SecurityException("Security guard or admin access required.");
        }
    }

    private void validateSecurityGuard(String token) {
        User user = authenticatedUser(token);
        if (user.getRole() != UserRole.SECURITY_GUARD) {
            throw new SecurityException("Security guard access required.");
        }
    }

    private User authenticatedUser(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new SecurityException("Invalid or expired session token.");
        }

        return userRepository.findBySessionToken(token.trim())
                .orElseThrow(() -> new SecurityException("Invalid or expired session token."));
    }

    private void validateVisitor(Visitor visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("Visitor details are required.");
        }
        if (visitor.getVisitorName() == null || visitor.getVisitorName().trim().isEmpty()) {
            throw new IllegalArgumentException("Visitor name is required.");
        }

        if (visitor.getPhone() == null || visitor.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        if (visitor.getPurpose() == null || visitor.getPurpose().trim().isEmpty()) {
            throw new IllegalArgumentException("Purpose of visit is required.");
        }
    }
}