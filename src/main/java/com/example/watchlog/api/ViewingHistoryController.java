package com.example.watchlog.api;

import com.example.watchlog.api.dto.ViewingHistoryDtos.CreateViewingHistoryRequest;
import com.example.watchlog.api.dto.ViewingHistoryDtos.UpdateViewingHistoryRequest;
import com.example.watchlog.api.dto.ViewingHistoryDtos.ViewingHistoryResponse;
import com.example.watchlog.service.ViewingHistoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/viewing-histories")
public class ViewingHistoryController {
    private final ViewingHistoryService historyService;

    public ViewingHistoryController(ViewingHistoryService historyService) { this.historyService = historyService; }

    @GetMapping
    public List<ViewingHistoryResponse> findAll(@PathVariable Long userId, HttpSession session) {
        AuthController.requireOwner(session, userId);
        return historyService.findAll(userId);
    }

    @PostMapping
    public ResponseEntity<ViewingHistoryResponse> create(@PathVariable Long userId,
                                                         @Valid @RequestBody CreateViewingHistoryRequest request,
                                                         HttpSession session) {
        AuthController.requireOwner(session, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(historyService.create(userId, request));
    }

    @GetMapping("/{id}")
    public ViewingHistoryResponse findOne(@PathVariable Long userId, @PathVariable Long id, HttpSession session) {
        AuthController.requireOwner(session, userId);
        return historyService.findOne(userId, id);
    }

    @PatchMapping("/{id}")
    public ViewingHistoryResponse update(@PathVariable Long userId, @PathVariable Long id,
                                         @Valid @RequestBody UpdateViewingHistoryRequest request,
                                         HttpSession session) {
        AuthController.requireOwner(session, userId);
        return historyService.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long userId, @PathVariable Long id, HttpSession session) {
        AuthController.requireOwner(session, userId);
        historyService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
