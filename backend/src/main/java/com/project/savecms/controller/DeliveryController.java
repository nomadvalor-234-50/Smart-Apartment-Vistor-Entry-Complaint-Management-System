package com.project.savecms.controller;

import com.project.savecms.entity.Delivery;
import com.project.savecms.service.DeliveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping("/qr-code")
    public ResponseEntity<?> generateDeliveryQr(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> request) {

        try {
            Delivery delivery = new Delivery();
            delivery.setDeliveryPersonName((String) request.get("deliveryPersonName"));
            delivery.setPhone((String) request.get("phone"));
            delivery.setDeliveryCompany((String) request.get("deliveryCompany"));

            Delivery savedDelivery = deliveryService.generateDeliveryQr(
                    delivery, getFlatId(request), extractToken(authorization));

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response("Delivery QR code generated successfully.", savedDelivery));

        } catch (SecurityException e) {
            return securityResponse(e);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllDeliveries(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        try {
            List<Delivery> deliveries = deliveryService.getAllDeliveries(extractToken(authorization));

            return ResponseEntity.ok(Map.of(
                    "message", "Deliveries retrieved successfully.",
                    "deliveries", deliveries.stream()
                            .map(delivery -> response("", delivery))
                            .toList()
            ));

        } catch (SecurityException e) {
            return securityResponse(e);
        }
    }

    @PutMapping("/{deliveryId}/verify")
    public ResponseEntity<?> verifyDelivery(
            @PathVariable Long deliveryId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        try {
            Delivery delivery = deliveryService.verifyDelivery(
                    deliveryId, extractToken(authorization));

            return ResponseEntity.ok(
                    response("Delivery QR code verified successfully.", delivery));

        } catch (SecurityException e) {
            return securityResponse(e);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private Long getFlatId(Map<String, Object> request) {
        Object flatId = request.get("flatId");

        if (flatId instanceof Number) {
            return ((Number) flatId).longValue();
        }

        return Long.parseLong(String.valueOf(flatId));
    }

    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new SecurityException("Invalid or expired session token.");
        }

        return authorization.substring(7);
    }

    private ResponseEntity<?> securityResponse(SecurityException e) {
        if (e.getMessage().equals("Admin access required.")
                || e.getMessage().equals("Security guard or admin access required.")) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", e.getMessage()));
    }

    private Map<String, Object> response(String message, Delivery delivery) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (!message.isEmpty()) {
            response.put("message", message);
        }

        response.put("deliveryId", delivery.getId());
        response.put("deliveryPersonName", delivery.getDeliveryPersonName());
        response.put("phone", delivery.getPhone());
        response.put("deliveryCompany", delivery.getDeliveryCompany());
        response.put("flatId", delivery.getFlat().getId());
        response.put("flatNumber", delivery.getFlat().getFlatNumber());
        response.put("qrCode", delivery.getQrCode());
        response.put("status", delivery.getStatus());

        return response;
    }
}