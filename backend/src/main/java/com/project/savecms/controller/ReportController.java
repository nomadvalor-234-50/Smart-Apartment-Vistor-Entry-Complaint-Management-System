package com.project.savecms.controller;

import com.project.savecms.entity.Complaint;
import com.project.savecms.entity.Delivery;
import com.project.savecms.entity.Visitor;
import com.project.savecms.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/visitor-history")
    public ResponseEntity<?> getVisitorHistory(
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {

        try {
            List<Visitor> visitors = reportService
                    .getVisitorHistory(extractToken(authorization));

            return ResponseEntity.ok(Map.of(
                    "message", "Visitor history retrieved successfully.",
                    "visitors", visitors.stream()
                            .map(this::visitorResponse)
                            .toList()
            ));

        } catch (SecurityException e) {
            return securityResponse(e);
        }
    }

    @GetMapping("/delivery-history")
    public ResponseEntity<?> getDeliveryHistory(
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {

        try {
            List<Delivery> deliveries = reportService
                    .getDeliveryHistory(extractToken(authorization));

            return ResponseEntity.ok(Map.of(
                    "message", "Delivery history retrieved successfully.",
                    "deliveries", deliveries.stream()
                            .map(this::deliveryResponse)
                            .toList()
            ));

        } catch (SecurityException e) {
            return securityResponse(e);
        }
    }

    @GetMapping("/complaint-overview")
    public ResponseEntity<?> getComplaintOverview(
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {

        try {
            List<Complaint> complaints = reportService
                    .getComplaintOverview(extractToken(authorization));

            long openComplaints = complaints.stream()
                    .filter(complaint -> "OPEN".equals(complaint.getStatus()))
                    .count();

            long inProgressComplaints = complaints.stream()
                    .filter(complaint -> "IN_PROGRESS".equals(complaint.getStatus()))
                    .count();

            long resolvedComplaints = complaints.stream()
                    .filter(complaint -> "RESOLVED".equals(complaint.getStatus()))
                    .count();

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Complaint overview retrieved successfully.");
            response.put("totalComplaints", complaints.size());
            response.put("openComplaints", openComplaints);
            response.put("inProgressComplaints", inProgressComplaints);
            response.put("resolvedComplaints", resolvedComplaints);
            response.put("complaints", complaints.stream()
                    .map(this::complaintResponse)
                    .toList());

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return securityResponse(e);
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

    private Map<String, Object> visitorResponse(Visitor visitor) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("visitorId", visitor.getId());
        response.put("visitorName", visitor.getVisitorName());
        response.put("phone", visitor.getPhone());
        response.put("purpose", visitor.getPurpose());
        response.put("flatNumber", visitor.getFlat().getFlatNumber());
        response.put("entryTime", visitor.getEntryTime());
        response.put("exitTime", visitor.getExitTime());
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

    private Map<String, Object> complaintResponse(Complaint complaint) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("complaintId", complaint.getId());
        response.put("title", complaint.getTitle());
        response.put("category", complaint.getCategory());
        response.put("priority", complaint.getPriority());
        response.put("status", complaint.getStatus());
        response.put("flatNumber", complaint.getFlat().getFlatNumber());
        response.put("raisedBy", complaint.getRaisedBy().getFullName());
        response.put("createdAt", complaint.getCreatedAt());
        return response;
    }
}