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

    public Visitor recordEntry(Visitor visitor, Long flatId, String token) {
        validateSecurityOrAdmin(token);
        validateVisitor(visitor);

        Flat flat = flatRepository.findById(flatId)
                .orElseThrow(() -> new IllegalArgumentException("Flat not found."));

        visitor.setFlat(flat);
        visitor.setEntryTime(LocalDateTime.now());
        visitor.setStatus("IN");

        return visitorRepository.save(visitor);
    }

    public List<Visitor> getAllVisitors(String token) {
        validateSecurityOrAdmin(token);
        return visitorRepository.findAll();
    }

    public Visitor recordExit(Long visitorId, String token) {
        validateSecurityOrAdmin(token);

        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new IllegalArgumentException("Visitor entry not found."));

        if ("OUT".equals(visitor.getStatus())) {
            throw new IllegalArgumentException("Visitor has already exited.");
        }

        visitor.setExitTime(LocalDateTime.now());
        visitor.setStatus("OUT");

        return visitorRepository.save(visitor);
    }

    private void validateSecurityOrAdmin(String token) {
        User user = userRepository.findBySessionToken(token)
                .orElseThrow(() -> new SecurityException("Invalid or expired session token."));

        if (user.getRole() != UserRole.ADMIN
                && user.getRole() != UserRole.SECURITY_GUARD) {
            throw new SecurityException("Security guard or admin access required.");
        }
    }

    private void validateVisitor(Visitor visitor) {
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