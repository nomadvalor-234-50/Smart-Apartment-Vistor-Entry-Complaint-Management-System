package com.project.savecms.controller;

import com.project.savecms.entity.Visitor;
import com.project.savecms.service.VisitorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visitors")
public class VisitorController {

    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @PostMapping("/entry")
    public ResponseEntity<?> recordEntry(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> request) {

        try {
            Visitor visitor = new Visitor();
            visitor.setVisitorName((String) request.get("visitorName"));
            visitor.setPhone((String) request.get("phone"));
            visitor.setPurpose((String) request.get("purpose"));

            Long flatId = getFlatId(request);

            Visitor savedVisitor = visitorService.recordEntry(
                    visitor, flatId, extractToken(authorization));

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response("Visitor entry recorded successfully.", savedVisitor));

        } catch (SecurityException e) {
            return securityResponse(e);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllVisitors(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        try {
            List<Visitor> visitors = visitorService.getAllVisitors(extractToken(authorization));

            return ResponseEntity.ok(Map.of(
                    "message", "Visitors retrieved successfully.",
                    "visitors", visitors.stream()
                            .map(visitor -> response("", visitor))
                            .toList()
            ));

        } catch (SecurityException e) {
            return securityResponse(e);
        }
    }

    @PutMapping("/{visitorId}/exit")
    public ResponseEntity<?> recordExit(
            @PathVariable Long visitorId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        try {
            Visitor visitor = visitorService.recordExit(visitorId, extractToken(authorization));

            return ResponseEntity.ok(
                    response("Visitor exit recorded successfully.", visitor));

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
        if (e.getMessage().equals("Security guard or admin access required.")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", e.getMessage()));
    }

    private Map<String, Object> response(String message, Visitor visitor) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (!message.isEmpty()) {
            response.put("message", message);
        }

        response.put("visitorId", visitor.getId());
        response.put("visitorName", visitor.getVisitorName());
        response.put("phone", visitor.getPhone());
        response.put("purpose", visitor.getPurpose());
        response.put("flatId", visitor.getFlat().getId());
        response.put("flatNumber", visitor.getFlat().getFlatNumber());
        response.put("entryTime", visitor.getEntryTime());
        response.put("exitTime", visitor.getExitTime());
        response.put("status", visitor.getStatus());

        return response;
    }
}