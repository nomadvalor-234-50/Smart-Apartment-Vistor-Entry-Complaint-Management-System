package com.project.savecms.controller;

import com.project.savecms.entity.Flat;
import com.project.savecms.service.QRService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/flats")
public class QRController {

    private final QRService qrService;

    public QRController(QRService qrService) {
        this.qrService = qrService;
    }

    @PostMapping("/qr-code/validate")
    public ResponseEntity<?> validateQrCode(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, String> request) {
        try {
            Flat flat = qrService.validateFlatQrCode(
                    request == null ? null : request.get("qrCode"),
                    extractToken(authorization));

            return ResponseEntity.ok(response(
                    "Flat QR code validated successfully.", flat));

        } catch (SecurityException e) {
            return securityResponse(e);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{flatId}/qr-code")
    public ResponseEntity<?> generateQrCode(
            @PathVariable Long flatId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        try {
            Flat flat = qrService.generateFlatQrCode(flatId, extractToken(authorization));

            return ResponseEntity.ok(response(
                    "Flat QR code generated successfully.", flat));

        } catch (SecurityException e) {
            return securityResponse(e);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{flatId}/qr-code")
    public ResponseEntity<?> getQrCode(
            @PathVariable Long flatId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        try {
            Flat flat = qrService.getFlatQrCode(flatId, extractToken(authorization));

            return ResponseEntity.ok(response(
                    "Flat QR code retrieved successfully.", flat));

        } catch (SecurityException e) {
            return securityResponse(e);
        } catch (IllegalArgumentException e) {
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
        if (e.getMessage().equals("Admin access required.")
                || e.getMessage().equals("Security guard or admin access required.")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", e.getMessage()));
    }

    private Map<String, Object> response(String message, Flat flat) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("message", message);
        response.put("flatId", flat.getId());
        response.put("flatNumber", flat.getFlatNumber());
        response.put("blockName", flat.getBlockName());
        response.put("floorNumber", flat.getFloorNumber());
        response.put("qrCode", flat.getQrCode());

        return response;
    }
}