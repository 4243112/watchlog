package com.example.watchlog.repository;

import com.example.watchlog.domain.StreamingService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamingServiceRepository extends JpaRepository<StreamingService, Long> {
    boolean existsByNameIgnoreCase(String name);
}
