package com.project.savecms.service;

import com.project.savecms.entity.Delivery;
import com.project.savecms.entity.Flat;
import com.project.savecms.entity.Resident;
import com.project.savecms.entity.User;
import com.project.savecms.entity.UserRole;
import com.project.savecms.entity.Visitor;
import com.project.savecms.repository.DeliveryRepository;
import com.project.savecms.repository.FlatRepository;
import com.project.savecms.repository.ResidentRepository;
import com.project.savecms.repository.UserRepository;
import com.project.savecms.repository.VisitorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GlobalSearchService {

    private final FlatRepository flatRepository;
    private final ResidentRepository residentRepository;
    private final VisitorRepository visitorRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;

    public GlobalSearchService(FlatRepository flatRepository,
                               ResidentRepository residentRepository,
                               VisitorRepository visitorRepository,
                               DeliveryRepository deliveryRepository,
                               UserRepository userRepository) {
        this.flatRepository = flatRepository;
        this.residentRepository = residentRepository;
        this.visitorRepository = visitorRepository;
        this.deliveryRepository = deliveryRepository;
        this.userRepository = userRepository;
    }

    public List<Flat> searchFlats(String keyword, String token) {
        validateAdmin(token);
        String searchText = validateAndGetSearchText(keyword);

        return flatRepository.findAll().stream()
                .filter(flat -> contains(flat.getFlatNumber(), searchText)
                        || contains(flat.getBlockName(), searchText))
                .toList();
    }

    public List<Resident> searchResidents(String keyword, String token) {
        validateAdmin(token);
        String searchText = validateAndGetSearchText(keyword);

        return residentRepository.findAll().stream()
                .filter(resident -> contains(resident.getFullName(), searchText)
                        || contains(resident.getEmail(), searchText)
                        || contains(resident.getPhone(), searchText)
                        || contains(resident.getFlat().getFlatNumber(), searchText))
                .toList();
    }

    public List<Visitor> searchVisitors(String keyword, String token) {
        validateAdmin(token);
        String searchText = validateAndGetSearchText(keyword);

        return visitorRepository.findAll().stream()
                .filter(visitor -> contains(visitor.getVisitorName(), searchText)
                        || contains(visitor.getPhone(), searchText)
                        || contains(visitor.getPurpose(), searchText)
                        || contains(visitor.getFlat().getFlatNumber(), searchText))
                .toList();
    }

    public List<Delivery> searchDeliveries(String keyword, String token) {
        validateAdmin(token);
        String searchText = validateAndGetSearchText(keyword);

        return deliveryRepository.findAll().stream()
                .filter(delivery -> contains(delivery.getDeliveryPersonName(), searchText)
                        || contains(delivery.getPhone(), searchText)
                        || contains(delivery.getDeliveryCompany(), searchText)
                        || contains(delivery.getFlat().getFlatNumber(), searchText))
                .toList();
    }

    private String validateAndGetSearchText(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword is required.");
        }

        return keyword.trim().toLowerCase();
    }

    private boolean contains(String value, String searchText) {
        return value != null && value.toLowerCase().contains(searchText);
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