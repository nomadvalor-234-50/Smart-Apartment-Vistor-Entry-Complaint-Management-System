package com.project.savecms.service;

import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.repository.ComplaintRepository;
import com.project.savecms.repository.DeliveryRepository;
import com.project.savecms.repository.FlatRepository;
import com.project.savecms.repository.ResidentRepository;
import com.project.savecms.repository.UserRepository;
import com.project.savecms.repository.VisitorRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final FlatRepository flatRepository;
    private final ResidentRepository residentRepository;
    private final VisitorRepository visitorRepository;
    private final DeliveryRepository deliveryRepository;
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    public DashboardService(FlatRepository flatRepository,
                            ResidentRepository residentRepository,
                            VisitorRepository visitorRepository,
                            DeliveryRepository deliveryRepository,
                            ComplaintRepository complaintRepository,
                            UserRepository userRepository) {
        this.flatRepository = flatRepository;
        this.residentRepository = residentRepository;
        this.visitorRepository = visitorRepository;
        this.deliveryRepository = deliveryRepository;
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getDashboardSummary(String token) {
        validateAdmin(token);

        long activeVisitors = visitorRepository.findAll().stream()
                .filter(visitor -> "IN".equals(visitor.getStatus()))
                .count();

        long pendingDeliveries = deliveryRepository.findAll().stream()
                .filter(delivery -> "PENDING".equals(delivery.getStatus()))
                .count();

        long openComplaints = complaintRepository.findAll().stream()
                .filter(complaint -> "OPEN".equals(complaint.getStatus()))
                .count();

        long inProgressComplaints = complaintRepository.findAll().stream()
                .filter(complaint -> "IN_PROGRESS".equals(complaint.getStatus()))
                .count();

        long resolvedComplaints = complaintRepository.findAll().stream()
                .filter(complaint -> "RESOLVED".equals(complaint.getStatus()))
                .count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("message", "Dashboard summary retrieved successfully.");
        summary.put("totalFlats", flatRepository.count());
        summary.put("totalResidents", residentRepository.count());
        summary.put("totalVisitors", visitorRepository.count());
        summary.put("activeVisitors", activeVisitors);
        summary.put("totalDeliveries", deliveryRepository.count());
        summary.put("pendingDeliveries", pendingDeliveries);
        summary.put("totalComplaints", complaintRepository.count());
        summary.put("openComplaints", openComplaints);
        summary.put("inProgressComplaints", inProgressComplaints);
        summary.put("resolvedComplaints", resolvedComplaints);

        return summary;
    }

    private void validateAdmin(String token) {
        User user = userRepository.findBySessionToken(token)
                .orElseThrow(() ->
                        new SecurityException("Invalid or expired session token."));

        if (user.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Admin access required.");
        }
    }
}