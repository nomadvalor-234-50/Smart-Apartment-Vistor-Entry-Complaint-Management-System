package com.project.savecms.controller;

import com.project.savecms.entity.Complaint;
import com.project.savecms.service.ComplaintService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping
    public ResponseEntity<?> raiseComplaint(
            @RequestHeader(value = "Authorization", required = false)
            String authorization,
            @RequestBody Map<String, Object> request) {

        try {
            Complaint complaint = new Complaint();
            complaint.setTitle((String) request.get("title"));
            complaint.setDescription((String) request.get("description"));
            complaint.setCategory((String) request.get("category"));
            complaint.setPriority((String) request.get("priority"));

            Complaint savedComplaint = complaintService.raiseComplaint(
                    complaint, getFlatId(request), extractToken(authorization));

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response("Complaint raised successfully.", savedComplaint));

        } catch (SecurityException e) {
            return securityResponse(e);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyComplaints(
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {

        try {
            List<Complaint> complaints = complaintService
                    .getMyComplaints(extractToken(authorization));

            return ResponseEntity.ok(Map.of(
                    "message", "Your complaints retrieved successfully.",
                    "complaints", complaints.stream()
                            .map(complaint -> response("", complaint))
                            .toList()
            ));

        } catch (SecurityException e) {
            return securityResponse(e);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getComplaintsByStatus(
            @PathVariable String status,
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {

        try {
            List<Complaint> complaints = complaintService
                    .getComplaintsByStatus(status, extractToken(authorization));

            return ResponseEntity.ok(Map.of(
                    "message", "Complaints with status " + status + " retrieved successfully.",
                    "complaints", complaints.stream()
                            .map(complaint -> response("", complaint))
                            .toList()
            ));

        } catch (SecurityException e) {
            return securityResponse(e);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllComplaints(
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {

        try {
            List<Complaint> complaints = complaintService
                    .getAllComplaints(extractToken(authorization));

            return ResponseEntity.ok(Map.of(
                    "message", "All complaints retrieved successfully.",
                    "complaints", complaints.stream()
                            .map(complaint -> response("", complaint))
                            .toList()
            ));

        } catch (SecurityException e) {
            return securityResponse(e);
        }
    }

    @PutMapping("/{complaintId}/status")
    public ResponseEntity<?> updateComplaintStatus(
            @PathVariable Long complaintId,
            @RequestHeader(value = "Authorization", required = false)
            String authorization,
            @RequestBody Map<String, String> request) {

        try {
            Complaint complaint = complaintService.updateComplaintStatus(
                    complaintId,
                    request.get("status"),
                    extractToken(authorization));

            return ResponseEntity.ok(response(
                    "Complaint status updated successfully.", complaint));

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
        if (e.getMessage().equals("Resident access required.")
                || e.getMessage().equals("Admin access required.")) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", e.getMessage()));
    }

    private Map<String, Object> response(String message, Complaint complaint) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (!message.isEmpty()) {
            response.put("message", message);
        }

        response.put("complaintId", complaint.getId());
        response.put("title", complaint.getTitle());
        response.put("description", complaint.getDescription());
        response.put("category", complaint.getCategory());
        response.put("priority", complaint.getPriority());
        response.put("status", complaint.getStatus());
        response.put("createdAt", complaint.getCreatedAt());
        response.put("flatId", complaint.getFlat().getId());
        response.put("flatNumber", complaint.getFlat().getFlatNumber());
        response.put("raisedBy", complaint.getRaisedBy().getFullName());

        return response;
    }
}