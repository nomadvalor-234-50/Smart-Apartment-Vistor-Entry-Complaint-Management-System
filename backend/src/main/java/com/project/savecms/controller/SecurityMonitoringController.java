package com.project.savecms.controller;

import com.project.savecms.entity.Delivery;
import com.project.savecms.entity.Visitor;
import com.project.savecms.service.SecurityMonitoringService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security-monitoring")
public class SecurityMonitoringController {

    private final SecurityMonitoringService securityMonitoringService;

    public SecurityMonitoringController(
            SecurityMonitoringService securityMonitoringService) {
        this.securityMonitoringService = securityMonitoringService;
    }

    @GetMapping("/active-visitors")
    public ResponseEntity<?> getActiveVisitors(
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {

        try {
            List<Visitor> visitors = securityMonitoringService
                    .getActiveVisitors(extractToken(authorization));

            return ResponseEntity.ok(Map.of(
                    "message", "Active visitors retrieved successfully.",
                    "activeVisitors", visitors.stream()
                            .map(this::visitorResponse)
                            .toList()
            ));

        } catch (SecurityException e) {
            return securityResponse(e);
        }
    }

    @GetMapping("/pending-deliveries")
    public ResponseEntity<?> getPendingDeliveries(
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {

        try {
            List<Delivery> deliveries = securityMonitoringService
                    .getPendingDeliveries(extractToken(authorization));

            return ResponseEntity.ok(Map.of(
                    "message", "Pending deliveries retrieved successfully.",
                    "pendingDeliveries", deliveries.stream()
                            .map(this::deliveryResponse)
                            .toList()
            ));

        } catch (SecurityException e) {
            return securityResponse(e);
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSecuritySummary(
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {

        try {
            String token = extractToken(authorization);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Security summary retrieved successfully.");
            response.put("activeVisitors",
                    securityMonitoringService.getActiveVisitorCount(token));
            response.put("pendingDeliveries",
                    securityMonitoringService.getPendingDeliveryCount(token));

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return securityResponse(e);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchSecurityActivity(
            @RequestParam String keyword,
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {

        try {
            String token = extractToken(authorization);

            List<Visitor> visitors = securityMonitoringService
                    .searchVisitors(keyword, token);

            List<Delivery> deliveries = securityMonitoringService
                    .searchDeliveries(keyword, token);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Security activity search completed successfully.");
            response.put("keyword", keyword);
            response.put("visitors", visitors.stream()
                    .map(this::visitorResponse)
                    .toList());
            response.put("deliveries", deliveries.stream()
                    .map(this::deliveryResponse)
                    .toList());

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return securityResponse(e);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new SecurityException("Invalid or expired session token.");
        }

        return authorization.substring(7);
    }

    private ResponseEntity<?> securityResponse(SecurityException e) {
        if (e.getMessage().equals("Security guard or admin access required.")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", e.getMessage()));
    }

    private Map<String, Object> visitorResponse(Visitor visitor) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("visitorId", visitor.getId());
        response.put("visitorName", visitor.getVisitorName());
        response.put("phone", visitor.getPhone());
        response.put("purpose", visitor.getPurpose());
        response.put("flatNumber", visitor.getFlat().getFlatNumber());
        response.put("entryTime", visitor.getEntryTime());
        response.put("status", visitor.getStatus());
        return response;
    }

    private Map<String, Object> deliveryResponse(Delivery delivery) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deliveryId", delivery.getId());
        response.put("deliveryPersonName", delivery.getDeliveryPersonName());
        response.put("phone", delivery.getPhone());
        response.put("deliveryCompany", delivery.getDeliveryCompany());
        response.put("flatNumber", delivery.getFlat().getFlatNumber());
        response.put("qrCode", delivery.getQrCode());
        response.put("status", delivery.getStatus());
        return response;
    }
}