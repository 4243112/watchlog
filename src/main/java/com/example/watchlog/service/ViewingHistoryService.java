package com.example.watchlog.service;

import com.example.watchlog.api.ApiException;
import com.example.watchlog.api.dto.ViewingHistoryDtos.CreateViewingHistoryRequest;
import com.example.watchlog.api.dto.ViewingHistoryDtos.UpdateViewingHistoryRequest;
import com.example.watchlog.api.dto.ViewingHistoryDtos.ViewingHistoryResponse;
import com.example.watchlog.api.dto.ViewingHistoryDtos.WorkSummary;
import com.example.watchlog.domain.AppUser;
import com.example.watchlog.domain.ViewingHistory;
import com.example.watchlog.domain.Work;
import com.example.watchlog.repository.ViewingHistoryRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ViewingHistoryService {
    private final ViewingHistoryRepository historyRepository;
    private final AuthService authService;
    private final WorkService workService;

    public ViewingHistoryService(ViewingHistoryRepository historyRepository, AuthService authService,
                                 WorkService workService) {
        this.historyRepository = historyRepository;
        this.authService = authService;
        this.workService = workService;
    }

    @Transactional(readOnly = true)
    public List<ViewingHistoryResponse> findAll(Long userId) {
        authService.requireEntity(userId);
        return historyRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ViewingHistoryResponse findOne(Long userId, Long id) {
        return toResponse(requireHistory(userId, id));
    }

    public ViewingHistoryResponse create(Long userId, CreateViewingHistoryRequest request) {
        AppUser user = authService.requireEntity(userId);
        Work work = workService.requireWork(request.workId());
        if (historyRepository.existsByUserIdAndWorkId(userId, request.workId())) {
            throw new ApiException(HttpStatus.CONFLICT, "VIEWING_HISTORY_ALREADY_EXISTS", "この作品の視聴履歴はすでに登録されています");
        }
        ViewingHistory history = historyRepository.save(new ViewingHistory(user, work, request.status(),
                request.watchedAt(), request.rating(), clean(request.comment())));
        return toResponse(history);
    }

    public ViewingHistoryResponse update(Long userId, Long id, UpdateViewingHistoryRequest request) {
        ViewingHistory history = requireHistory(userId, id);
        history.update(request.status(), request.watchedAt(), request.rating(), clean(request.comment()));
        return toResponse(history);
    }

    public void delete(Long userId, Long id) {
        historyRepository.delete(requireHistory(userId, id));
    }

    private ViewingHistory requireHistory(Long userId, Long id) {
        return historyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "VIEWING_HISTORY_NOT_FOUND", "指定された視聴履歴が見つかりません"));
    }

    private ViewingHistoryResponse toResponse(ViewingHistory history) {
        Work work = history.getWork();
        return new ViewingHistoryResponse(history.getId(), history.getUser().getId(),
                new WorkSummary(work.getId(), work.getTitle(), work.getImageUrl(), work.getGenre(), work.getReleaseYear()),
                history.getStatus(), history.getWatchedAt(), history.getRating(), history.getComment(),
                history.getCreatedAt(), history.getUpdatedAt());
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
