package com.project.savecms.repository;

import com.project.savecms.entity.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {

    List<Visitor> findAllByOrderByEntryTimeDesc();

    List<Visitor> findAllByStatusOrderByEntryTimeDesc(String status);
}