package com.project.savecms.controller;

import com.project.savecms.entity.Delivery;
import com.project.savecms.entity.Flat;
import com.project.savecms.entity.Resident;
import com.project.savecms.entity.Visitor;
import com.project.savecms.service.GlobalSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    public GlobalSearchController(GlobalSearchService globalSearchService) {
        this.globalSearchService = globalSearchService;
    }

    @GetMapping
    public ResponseEntity<?> globalSearch(
            @RequestParam String keyword,
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {

        try {
            String token = extractToken(authorization);

            List<Flat> flats = globalSearchService.searchFlats(keyword, token);
            List<Resident> residents = globalSearchService.searchResidents(keyword, token);
            List<Visitor> visitors = globalSearchService.searchVisitors(keyword, token);
            List<Delivery> deliveries = globalSearchService.searchDeliveries(keyword, token);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Global search completed successfully.");
            response.put("keyword", keyword);
            response.put("flats", flats.stream().map(this::flatResponse).toList());
            response.put("residents", residents.stream()
                    .map(this::residentResponse).toList());
            response.put("visitors", visitors.stream()
                    .map(this::visitorResponse).toList());
            response.put("deliveries", deliveries.stream()
                    .map(this::deliveryResponse).toList());

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
        if (e.getMessage().equals("Admin access required.")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", e.getMessage()));
    }

    private Map<String, Object> flatResponse(Flat flat) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("flatId", flat.getId());
        response.put("flatNumber", flat.getFlatNumber());
        response.put("blockName", flat.getBlockName());
        response.put("floorNumber", flat.getFloorNumber());
        response.put("numberOfRooms", flat.getNumberOfRooms());
        return response;
    }

    private Map<String, Object> residentResponse(Resident resident) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("residentId", resident.getId());
        response.put("fullName", resident.getFullName());
        response.put("email", resident.getEmail());
        response.put("phone", resident.getPhone());
        response.put("flatNumber", resident.getFlat().getFlatNumber());
        return response;
    }

    private Map<String, Object> visitorResponse(Visitor visitor) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("visitorId", visitor.getId());
        response.put("visitorName", visitor.getVisitorName());
        response.put("phone", visitor.getPhone());
        response.put("purpose", visitor.getPurpose());
        response.put("flatNumber", visitor.getFlat().getFlatNumber());
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
        response.put("status", delivery.getStatus());
        return response;
    }
}