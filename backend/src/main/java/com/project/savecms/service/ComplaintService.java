package com.project.savecms.service;

import com.project.savecms.entity.Complaint;
import com.project.savecms.entity.Flat;
import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.repository.ComplaintRepository;
import com.project.savecms.repository.FlatRepository;
import com.project.savecms.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final FlatRepository flatRepository;
    private final UserRepository userRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
                            FlatRepository flatRepository,
                            UserRepository userRepository) {
        this.complaintRepository = complaintRepository;
        this.flatRepository = flatRepository;
        this.userRepository = userRepository;
    }

    public Complaint raiseComplaint(Complaint complaint, Long flatId, String token) {
        User user = validateResidentOrManager(token);
        validateComplaint(complaint);

        Flat flat = flatRepository.findById(flatId)
                .orElseThrow(() -> new IllegalArgumentException("Flat not found."));

        complaint.setFlat(flat);
        complaint.setRaisedBy(user);
        complaint.setStatus("OPEN");
        complaint.setCreatedAt(LocalDateTime.now());

        return complaintRepository.save(complaint);
    }

    public List<Complaint> getMyComplaints(String token) {
        User user = validateResidentOrManager(token);
        return complaintRepository.findByRaisedBy(user);
    }

    public List<Complaint> getAllComplaints(String token) {
        validateAdmin(token);
        return complaintRepository.findAll();
    }

    public Complaint updateComplaintStatus(Long complaintId, String status,
                                           String token) {
        validateAdmin(token);
        validateComplaintStatus(status);

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found."));

        complaint.setStatus(status);
        return complaintRepository.save(complaint);
    }

    public List<Complaint> getComplaintsByStatus(String status, String token) {
        validateAdmin(token);
        validateComplaintStatus(status);

        return complaintRepository.findByStatus(status);
    }

    private User validateResidentOrManager(String token) {
        User user = userRepository.findBySessionToken(token)
                .orElseThrow(() -> new SecurityException("Invalid or expired session token."));

        if (user.getRole() != UserRole.RESIDENT
                && user.getRole() != UserRole.APARTMENT_MANAGER) {
            throw new SecurityException("Resident access required.");
        }

        return user;
    }

    private void validateAdmin(String token) {
        User user = userRepository.findBySessionToken(token)
                .orElseThrow(() -> new SecurityException("Invalid or expired session token."));

        if (user.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Admin access required.");
        }
    }

    private void validateComplaintStatus(String status) {
        if (!"OPEN".equals(status)
                && !"IN_PROGRESS".equals(status)
                && !"RESOLVED".equals(status)) {
            throw new IllegalArgumentException(
                    "Status must be OPEN, IN_PROGRESS, or RESOLVED.");
        }
    }

    private void validateComplaint(Complaint complaint) {
        if (complaint.getTitle() == null || complaint.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Complaint title is required.");
        }

        if (complaint.getDescription() == null
                || complaint.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Complaint description is required.");
        }

        if (complaint.getCategory() == null
                || complaint.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Complaint category is required.");
        }

        if (complaint.getPriority() == null
                || complaint.getPriority().trim().isEmpty()) {
            throw new IllegalArgumentException("Complaint priority is required.");
        }
    }
}