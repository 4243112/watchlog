package com.example.watchlog.repository;

import com.example.watchlog.domain.ViewingHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewingHistoryRepository extends JpaRepository<ViewingHistory, Long> {
    List<ViewingHistory> findByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<ViewingHistory> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndWorkId(Long userId, Long workId);
    void deleteByWorkId(Long workId);
}
