package com.project.savecms.service;

import com.project.savecms.entity.Complaint;
import com.project.savecms.entity.Delivery;
import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.entity.Visitor;
import com.project.savecms.repository.ComplaintRepository;
import com.project.savecms.repository.DeliveryRepository;
import com.project.savecms.repository.UserRepository;
import com.project.savecms.repository.VisitorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    private final VisitorRepository visitorRepository;
    private final DeliveryRepository deliveryRepository;
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    public ReportService(VisitorRepository visitorRepository,
                         DeliveryRepository deliveryRepository,
                         ComplaintRepository complaintRepository,
                         UserRepository userRepository) {
        this.visitorRepository = visitorRepository;
        this.deliveryRepository = deliveryRepository;
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
    }

    public List<Visitor> getVisitorHistory(String token) {
        validateAdmin(token);
        return visitorRepository.findAll();
    }

    public List<Delivery> getDeliveryHistory(String token) {
        validateAdmin(token);
        return deliveryRepository.findAll();
    }

    public List<Complaint> getComplaintOverview(String token) {
        validateAdmin(token);
        return complaintRepository.findAll();
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