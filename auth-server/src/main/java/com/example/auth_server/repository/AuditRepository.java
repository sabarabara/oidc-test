package com.example.auth_server.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.auth_server.model.AuditRecord;


@Repository
public interface AuditRepository extends JpaRepository<AuditRecord, Long> {
}
