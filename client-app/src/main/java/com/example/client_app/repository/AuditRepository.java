package com.example.client_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.client_app.model.AuditRecord;


@Repository
public interface AuditRepository extends JpaRepository<AuditRecord, Long> {
}
