package com.example.watchlog.repository;

import com.example.watchlog.domain.WorkStreamingAvailability;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkStreamingAvailabilityRepository extends JpaRepository<WorkStreamingAvailability, Long> {
    List<WorkStreamingAvailability> findByWorkIdOrderByStreamingServiceName(Long workId);
    boolean existsByWorkIdAndStreamingServiceId(Long workId, Long streamingServiceId);
    void deleteByWorkId(Long workId);
    void deleteByStreamingServiceId(Long streamingServiceId);
}
