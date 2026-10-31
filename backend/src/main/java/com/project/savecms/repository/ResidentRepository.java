package com.project.savecms.repository;

import com.project.savecms.entity.Resident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidentRepository extends JpaRepository<Resident, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}