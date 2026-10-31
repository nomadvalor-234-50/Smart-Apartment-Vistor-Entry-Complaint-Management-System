package com.project.savecms.service;

import com.project.savecms.entity.Delivery;
import com.project.savecms.entity.Flat;
import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.repository.DeliveryRepository;
import com.project.savecms.repository.FlatRepository;
import com.project.savecms.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final FlatRepository flatRepository;
    private final UserRepository userRepository;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           FlatRepository flatRepository,
                           UserRepository userRepository) {
        this.deliveryRepository = deliveryRepository;
        this.flatRepository = flatRepository;
        this.userRepository = userRepository;
    }

    public Delivery generateDeliveryQr(Delivery delivery, Long flatId, String token) {
        validateAdmin(token);
        validateDelivery(delivery);

        Flat flat = flatRepository.findById(flatId)
                .orElseThrow(() -> new IllegalArgumentException("Flat not found."));

        delivery.setFlat(flat);
        delivery.setQrCode("DELIVERY-" + UUID.randomUUID());
        delivery.setStatus("PENDING");

        return deliveryRepository.save(delivery);
    }

    public List<Delivery> getAllDeliveries(String token) {
        validateSecurityOrAdmin(token);
        return deliveryRepository.findAll();
    }

    public Delivery verifyDelivery(Long deliveryId, String token) {
        validateSecurityOrAdmin(token);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery record not found."));

        if ("VERIFIED".equals(delivery.getStatus())) {
            throw new IllegalArgumentException("Delivery QR code has already been verified.");
        }

        delivery.setStatus("VERIFIED");
        return deliveryRepository.save(delivery);
    }

    private void validateAdmin(String token) {
        User user = userRepository.findBySessionToken(token)
                .orElseThrow(() -> new SecurityException("Invalid or expired session token."));

        if (user.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Admin access required.");
        }
    }

    private void validateSecurityOrAdmin(String token) {
        User user = userRepository.findBySessionToken(token)
                .orElseThrow(() -> new SecurityException("Invalid or expired session token."));

        if (user.getRole() != UserRole.ADMIN
                && user.getRole() != UserRole.SECURITY_GUARD) {
            throw new SecurityException("Security guard or admin access required.");
        }
    }

    private void validateDelivery(Delivery delivery) {
        if (delivery.getDeliveryPersonName() == null
                || delivery.getDeliveryPersonName().trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery person name is required.");
        }

        if (delivery.getPhone() == null || delivery.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        if (delivery.getDeliveryCompany() == null
                || delivery.getDeliveryCompany().trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery company is required.");
        }
    }
}