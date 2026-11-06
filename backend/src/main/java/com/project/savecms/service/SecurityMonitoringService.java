package com.project.savecms.service;

import com.project.savecms.entity.Delivery;
import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.entity.Visitor;
import com.project.savecms.repository.DeliveryRepository;
import com.project.savecms.repository.UserRepository;
import com.project.savecms.repository.VisitorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecurityMonitoringService {

    private final VisitorRepository visitorRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;

    public SecurityMonitoringService(VisitorRepository visitorRepository,
                                     DeliveryRepository deliveryRepository,
                                     UserRepository userRepository) {
        this.visitorRepository = visitorRepository;
        this.deliveryRepository = deliveryRepository;
        this.userRepository = userRepository;
    }

    public List<Visitor> getActiveVisitors(String token) {
        validateSecurityOrAdmin(token);

        return visitorRepository.findAllByStatusOrderByEntryTimeDesc("IN");
    }

    public List<Delivery> getPendingDeliveries(String token) {
        validateSecurityOrAdmin(token);

        return deliveryRepository.findAll().stream()
                .filter(delivery -> "PENDING".equals(delivery.getStatus()))
                .toList();
    }

    public long getActiveVisitorCount(String token) {
        return getActiveVisitors(token).size();
    }

    public long getPendingDeliveryCount(String token) {
        return getPendingDeliveries(token).size();
    }

    public List<Visitor> searchVisitors(String keyword, String token) {
        validateSecurityOrAdmin(token);
        validateKeyword(keyword);

        String searchText = keyword.toLowerCase().trim();

        return visitorRepository.findAll().stream()
                .filter(visitor ->
                        contains(visitor.getVisitorName(), searchText)
                                || contains(visitor.getPhone(), searchText)
                                || contains(visitor.getPurpose(), searchText)
                                || contains(visitor.getStatus(), searchText)
                                || contains(visitor.getFlat().getFlatNumber(), searchText))
                .toList();
    }

    public List<Delivery> searchDeliveries(String keyword, String token) {
        validateSecurityOrAdmin(token);
        validateKeyword(keyword);

        String searchText = keyword.toLowerCase().trim();

        return deliveryRepository.findAll().stream()
                .filter(delivery ->
                        contains(delivery.getDeliveryPersonName(), searchText)
                                || contains(delivery.getPhone(), searchText)
                                || contains(delivery.getDeliveryCompany(), searchText)
                                || contains(delivery.getStatus(), searchText)
                                || contains(delivery.getFlat().getFlatNumber(), searchText))
                .toList();
    }

    private boolean contains(String value, String searchText) {
        return value != null && value.toLowerCase().contains(searchText);
    }

    private void validateKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword is required.");
        }
    }

    private void validateSecurityOrAdmin(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new SecurityException("Invalid or expired session token.");
        }

        User user = userRepository.findBySessionToken(token.trim())
                .orElseThrow(() -> new SecurityException("Invalid or expired session token."));

        if (user.getRole() != UserRole.ADMIN
                && user.getRole() != UserRole.SECURITY_GUARD) {
            throw new SecurityException("Security guard or admin access required.");
        }
    }
}