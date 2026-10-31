package com.project.savecms.controller;

import com.project.savecms.entity.Resident;
import com.project.savecms.service.ResidentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/residents")
public class ResidentController {

    private final ResidentService residentService;

    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    @PostMapping
    public ResponseEntity<?> createResident(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> request) {

        try {
            Resident resident = mapToResident(request);
            Long flatId = getFlatId(request);

            Resident savedResident = residentService.createResident(
                    resident, flatId, extractToken(authorization));

            return ResponseEntity.status(HttpStatus.CREATED).body(response(
                    "Resident created successfully.", savedResident));

        } catch (SecurityException e) {
            return securityResponse(e);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllResidents(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        try {
            List<Resident> residents = residentService.getAllResidents(extractToken(authorization));

            return ResponseEntity.ok(Map.of(
                    "message", "Residents retrieved successfully.",
                    "residents", residents.stream()
                            .map(resident -> response("", resident))
                            .toList()
            ));

        } catch (SecurityException e) {
            return securityResponse(e);
        }
    }

    @PutMapping("/{residentId}")
    public ResponseEntity<?> updateResident(
            @PathVariable Long residentId,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> request) {

        try {
            Resident resident = mapToResident(request);
            Long flatId = getFlatId(request);

            Resident updatedResident = residentService.updateResident(
                    residentId, resident, flatId, extractToken(authorization));

            return ResponseEntity.ok(response(
                    "Resident updated successfully.", updatedResident));

        } catch (SecurityException e) {
            return securityResponse(e);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private Resident mapToResident(Map<String, Object> request) {
        Resident resident = new Resident();
        resident.setFullName((String) request.get("fullName"));
        resident.setEmail((String) request.get("email"));
        resident.setPhone((String) request.get("phone"));
        resident.setEmergencyContact((String) request.get("emergencyContact"));

        Object occupants = request.get("numberOfOccupants");
        if (occupants instanceof Number) {
            resident.setNumberOfOccupants(((Number) occupants).intValue());
        }

        return resident;
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
        if (e.getMessage().equals("Admin access required.")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", e.getMessage()));
    }

    private Map<String, Object> response(String message, Resident resident) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (!message.isEmpty()) {
            response.put("message", message);
        }

        response.put("residentId", resident.getId());
        response.put("fullName", resident.getFullName());
        response.put("email", resident.getEmail());
        response.put("phone", resident.getPhone());
        response.put("emergencyContact", resident.getEmergencyContact());
        response.put("numberOfOccupants", resident.getNumberOfOccupants());
        response.put("flatId", resident.getFlat().getId());
        response.put("flatNumber", resident.getFlat().getFlatNumber());

        return response;
    }
}