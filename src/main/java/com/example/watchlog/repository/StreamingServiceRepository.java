package com.example.watchlog.repository;

import com.example.watchlog.domain.StreamingService;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamingServiceRepository extends JpaRepository<StreamingService, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<StreamingService> findByNameIgnoreCase(String name);
}
