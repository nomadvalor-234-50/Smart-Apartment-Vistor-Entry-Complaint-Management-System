package com.project.savecms.repository;

import com.project.savecms.entity.Flat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlatRepository extends JpaRepository<Flat, Long> {

    boolean existsByFlatNumber(String flatNumber);

    Optional<Flat> findByQrCode(String qrCode);
}