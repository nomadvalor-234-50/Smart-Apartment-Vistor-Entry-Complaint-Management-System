package com.project.savecms.repository;

import com.project.savecms.entity.Complaint;
import com.project.savecms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByRaisedBy(User raisedBy);

    List<Complaint> findByStatus(String status);
}