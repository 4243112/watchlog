package com.example.watchlog.repository;

import com.example.watchlog.domain.Work;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkRepository extends JpaRepository<Work, Long> {
    Optional<Work> findFirstByTitle(String title);

    Optional<Work> findByExternalSourceAndExternalIdAndExternalMediaType(
            String externalSource, Long externalId, String externalMediaType);
}
