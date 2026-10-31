package com.project.savecms.repository;

import com.project.savecms.entity.Flat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlatRepository extends JpaRepository<Flat, Long> {

    boolean existsByFlatNumber(String flatNumber);
}