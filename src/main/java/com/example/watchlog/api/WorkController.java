package com.example.watchlog.api;

import com.example.watchlog.api.dto.WorkDtos.AvailabilityRequest;
import com.example.watchlog.api.dto.WorkDtos.AvailabilityUpdateRequest;
import com.example.watchlog.api.dto.WorkDtos.StreamingOptionResponse;
import com.example.watchlog.api.dto.WorkDtos.WorkRequest;
import com.example.watchlog.api.dto.WorkDtos.WorkResponse;
import com.example.watchlog.service.WorkService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/works")
public class WorkController {
    private final WorkService workService;

    public WorkController(WorkService workService) { this.workService = workService; }

    @GetMapping
    public List<WorkResponse> findAll() { return workService.findAll(); }

    @PostMapping
    public ResponseEntity<WorkResponse> create(@Valid @RequestBody WorkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workService.create(request));
    }

    @GetMapping("/{id}")
    public WorkResponse findOne(@PathVariable Long id) { return workService.findOne(id); }

    @PutMapping("/{id}")
    public WorkResponse update(@PathVariable Long id, @Valid @RequestBody WorkRequest request) {
        return workService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{workId}/streaming-services")
    public ResponseEntity<StreamingOptionResponse> addAvailability(
            @PathVariable Long workId, @Valid @RequestBody AvailabilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workService.addAvailability(workId, request));
    }

    @PutMapping("/{workId}/streaming-services/{availabilityId}")
    public StreamingOptionResponse updateAvailability(@PathVariable Long workId, @PathVariable Long availabilityId,
                                                      @Valid @RequestBody AvailabilityUpdateRequest request) {
        return workService.updateAvailability(workId, availabilityId, request);
    }

    @DeleteMapping("/{workId}/streaming-services/{availabilityId}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long workId, @PathVariable Long availabilityId) {
        workService.deleteAvailability(workId, availabilityId);
        return ResponseEntity.noContent().build();
    }
}
